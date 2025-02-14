package tw.platform.sideProject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.platform.sideProject.model.AddMemberOrderRequest;
import tw.platform.sideProject.model.Member;
import tw.platform.sideProject.model.MemberOrder;
import tw.platform.sideProject.model.MemberOrderKey;
import tw.platform.sideProject.model.Order;
import tw.platform.sideProject.repository.MemberOrderRepository;
import tw.platform.sideProject.repository.MemberRepository;
import tw.platform.sideProject.repository.OrderRepository;

@Service
public class MemberOrderService {
    
    @Autowired
    private MemberOrderRepository memberOrderRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    // 新增會員創建專案的關聯 (owned設為true)
    public String addOwnedOrder(AddMemberOrderRequest request) {
    	
    	// 查找會員
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 查找專案
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        MemberOrder memberOrder = new MemberOrder();
        MemberOrderKey memberOrderKey = new MemberOrderKey();
        memberOrder.setId(memberOrderKey); // 初始化 ID
        memberOrder.getId().setMemberid(request.getMemberId());
        memberOrder.getId().setOrderid(request.getOrderId());
        memberOrder.setMember(member);  // 設置 member 對象
        memberOrder.setOrder(order);    // 設置 order 對象
        memberOrder.setOwned(true);
        memberOrder.setWanted(false);
        memberOrder.setCollected(false);
        memberOrderRepository.save(memberOrder);
        return "會員創建專案關聯已新增";
    }

    // 新增會員對專案有興趣的關聯 (wanted設為true)
    public String addWantedOrder(AddMemberOrderRequest request) {
    	
    	// 查找會員
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 查找專案
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        MemberOrder memberOrder = new MemberOrder();
        MemberOrderKey memberOrderKey = new MemberOrderKey();
        memberOrder.setId(memberOrderKey); // 初始化 ID
        memberOrder.setMember(member);  // 設置 member 對象
        memberOrder.setOrder(order);    // 設置 order 對象
        memberOrder.getId().setMemberid(request.getMemberId());
        memberOrder.getId().setOrderid(request.getOrderId());
        memberOrder.setOwned(false);
    	memberOrder.setWanted(true);
    	memberOrder.setCollected(false);
        
        memberOrderRepository.save(memberOrder);
        return "會員對專案有興趣的關聯已新增";
    }

    // 移除會員對專案有興趣的關聯 (wanted設為false)
    @Transactional
    public String removeWantedOrder(Long memberId, Long orderId) {
        // 查找指定會員和專案的關聯
        MemberOrder memberOrder = memberOrderRepository.findByMember_memberidAndOrder_orderid(memberId, orderId)
                .orElseThrow(() -> new RuntimeException("找不到該會員與專案的關聯"));

        // 如果目前該關聯已經不是 'wanted'，則不進行任何操作
        if (!memberOrder.getWanted()) {
            return "該會員對專案的關聯已經為非 wanted";
        }

        // 更新 wanted 為 false
        memberOrder.setWanted(false);
        memberOrderRepository.save(memberOrder);

        // 檢查並刪除所有 wanted 和 owned 都為 false 的資料
        memberOrderRepository.deleteAllByWantedFalseAndOwnedFalse();

        return "會員對專案的 wanted 關聯已移除，並刪除所有 wanted 和 owned 都為 false 的資料";
    }

    // 會員刪除專案 (僅限 owned = true 的會員執行)
    @Transactional
    public String deleteOwnedOrder(Long memberId, Long orderId) {
        MemberOrder memberOrder = memberOrderRepository.findByMember_memberidAndOrder_orderid(memberId, orderId)
                .orElseThrow(() -> new RuntimeException("找不到該會員與專案的關聯"));
        if (!memberOrder.getOwned()) {
            return "只有創建該專案的會員才能刪除專案";
        }
        memberOrderRepository.deleteAllByOrderid(orderId);
        return "專案及所有相關關聯已刪除";
    }
    
    // 查找某個會員相關的所有專案
    public List<Order> getOrdersByMemberId(Long memberId) {
        return memberOrderRepository.findAllOrdersByMemberid(memberId);
    }

    // 查找某個專案相關的所有會員
    public List<Member> getMembersByOrderId(Long orderId) {
        return memberOrderRepository.findAllMembersByOrderid(orderId);
    }
    
    // 查詢某個會員相關的所有專案的關係
    public List<MemberOrder> getAllMemberOrdersByMemberId(Long memberId) {
        return memberOrderRepository.findAllByMemberid(memberId);
    }
    
}