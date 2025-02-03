package tw.platform.sideProject.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.transaction.Transactional;
import tw.platform.sideProject.model.AddMemberOrderRequest;
import tw.platform.sideProject.model.Member;
import tw.platform.sideProject.model.MemberOrder;
import tw.platform.sideProject.model.Order;
import tw.platform.sideProject.repository.MemberOrderRepository;
import tw.platform.sideProject.service.MemberOrderService;

// Controller 只處理當 request 來用 response 回應 => 再給 Service 處理邏輯 => 再給 Dao 與資料庫溝通，存取物件
@RestController
@RequestMapping("/api/memberOrders")
@CrossOrigin(origins = "*") // 避免 CORS 問題
public class MemberOrderController {

	@Autowired
	private MemberOrderService memberOrderService;

	// 新增會員創建專案的關聯 (owned設為true)
	@PostMapping("/addOwnedOrder")
	public String addOwnedOrder(@RequestBody AddMemberOrderRequest request) {
		return memberOrderService.addOwnedOrder(request);
	}

	// 新增會員對專案有興趣的關聯 (wanted設為true)
	@PostMapping("/addWantedOrder")
	public String addWantedOrder(@RequestBody AddMemberOrderRequest request) {
		return memberOrderService.addWantedOrder(request);
	}

	// 移除會員對專案有興趣的關聯 (wanted設為false)
	@DeleteMapping("/removeWantedOrder/{memberId}/{orderId}")
	public String removeWantedOrder(@PathVariable Long memberid, @PathVariable Long orderid) {
		return memberOrderService.removeWantedOrder(memberid, orderid);
	}

	// 會員刪除專案 (僅限 owned = true 的會員執行)
	@DeleteMapping("/deleteOwnedOrder/{memberId}/{orderId}")
	public String deleteOwnedOrder(@PathVariable Long memberId, @PathVariable Long orderId) {
		return memberOrderService.deleteOwnedOrder(memberId, orderId);
	}

	// 查詢某個會員相關的所有專案
	@GetMapping("/getOrdersByMemberid/{memberid}")
	public List<MemberOrder> getOrdersByMemberid(@PathVariable Long memberid) throws IOException {
		// 查询所有对应 memberid 的订单
		List<MemberOrder> orders = memberOrderRepository.findAllById_Memberid(memberid);

		return orders;
	}

	// 查詢某個專案相關的所有會員
	@GetMapping("/getMembersByOrderid/{orderid}")
	public List<Member> getMembersByOrderId(@PathVariable Long orderid) {
		return memberOrderService.getMembersByOrderId(orderid);
	}

	// ======新增===================================
	@Autowired
	private MemberOrderRepository memberOrderRepository;

	// 根據 memberId 獲取該會員所有收藏的專案
	@GetMapping("/like/{memberid}")
	public List<MemberOrder> getwanted(@PathVariable Long memberid, Long orderid) {
		return memberOrderService.getcollectedByMemberId(memberid);
	}

	// 根據 memberId 和 orderId 獲取該會員對某個專案的收藏狀態（如：wanted）
	@GetMapping("/memberlike/{memberid}")
	public boolean getMemberCollected(@PathVariable Long memberid, @RequestParam Long orderid) {
		Optional<MemberOrder> memberOrder = memberOrderService.getOrderByMemberAndOrder(memberid, orderid);

		// 返回該會員對某個專案是否有收藏（wanted）
		return memberOrder.isPresent() && memberOrder.get().getCollected();
	}

	// 更新會員對專案的收藏狀態
	@Transactional
	@PostMapping("/updateWantedStatus")
	public void updateCollectedStatus(@RequestParam Long memberid, @RequestParam Long orderid,
			@RequestParam boolean collected) {
		memberOrderService.updateCollectedStatus(memberid, orderid, collected);
	}
	//--------------------------
	//站內信使用
	@PostMapping("/approve_submit")
    public ResponseEntity<String> approveOrder(@RequestParam Long orderId, 
                                               @RequestParam Long ownerId, 
                                               @RequestParam boolean isAgreed,Model model) {
        Order order = new Order();
        order.setOrderid(orderId);

        Member owner = new Member();
        owner.setMemberid(ownerId);
        
        MemberOrder ownerMember = new MemberOrder();
        ownerMember.setOwned(true); // 假設我們已經確認這個會員是擁有者

        memberOrderService.processAgreement(order, ownerMember, isAgreed);

        return ResponseEntity.ok("處理完成");
        
    }
	
}
