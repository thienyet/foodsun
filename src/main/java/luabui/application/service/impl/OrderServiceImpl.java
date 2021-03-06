package luabui.application.service.impl;

import luabui.application.constants.OrderStatus;
import luabui.application.dto.OrderDTO;
import luabui.application.dto.OrderFoodItemDTO;
import luabui.application.exception.*;
import luabui.application.model.*;
import luabui.application.repository.*;
import luabui.application.service.OrderService;
import luabui.application.utility.MapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private RestaurantRepository restaurantRepository;
    private FoodItemRepository foodItemRepository;
    private OrderFoodItemRepository orderFoodItemRepository;
    private DeliveryGuyRepository deliveryGuyRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository,
                            RestaurantRepository restaurantRepository, FoodItemRepository foodItemRepository,
                            OrderFoodItemRepository orderFoodItemRepository, DeliveryGuyRepository deliveryGuyRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.restaurantRepository = restaurantRepository;
        this.foodItemRepository = foodItemRepository;
        this.orderFoodItemRepository = orderFoodItemRepository;
        this.deliveryGuyRepository = deliveryGuyRepository;
    }

    @Override
    public List<OrderDTO> findAll() {
        return null;
    }

    @Override
    public OrderDTO findById(Long orderId) {
        return null;
    }

    @Override
    public OrderDTO save(OrderDTO orderDTO) {
//        log.debug("Saving order from Service.");
//        return orderRepository.save(newOrder);
        return null;
    }

//    public Order update(Order order, Long orderId) {
//        log.debug("Updating Order from Service.");
//        return null;
//    }

    @Override
    public void deleteById(Long orderId) {
//        log.debug("Deleting order by id from Service.");
//        orderRepository.deleteById(orderId);
    }

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        //TODO: break create order into multi small modules. If possible move error throwing in controller.
        log.debug("Create new order.");

        Long customerId = orderDTO.getCustomerId();
        Long restaurantId = orderDTO.getRestaurantId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        Order order = MapperUtil.toOrder(orderDTO);

        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryGuy(getDeliveryGuy());

        order.setDeliveryAddress(orderDTO.getDeliveryAddress());

        List<OrderFoodItem> orderFoodItems = orderDTO.getOrderFoodItemDTOs().stream()
                .map(orderFoodItemDTO -> toOrderFoodItem(orderFoodItemDTO)).collect(Collectors.toList());

        Double totalPrice = 0.0;

        for (OrderFoodItem orderFoodItem : orderFoodItems) {
            totalPrice += orderFoodItem.getPrice()*orderFoodItem.getQuantity();
        }

        if (BigDecimal.valueOf(totalPrice).compareTo(BigDecimal.valueOf(orderDTO.getTotalPrice())) != 0) {
            throw new PriceMismatchException("Total Price for this order should be " + totalPrice + " but found " + orderDTO.getTotalPrice());
        }

        order.setTotalPrice(totalPrice);
        order.setOrderStatus(OrderStatus.APPROVED);
        orderRepository.save(order);
        orderFoodItems.forEach(orderFoodItem -> orderFoodItem.setOrder(order));
        orderFoodItemRepository.saveAll(orderFoodItems);
        return MapperUtil.toOrderDTO(order);
    }

    @Override
    public OrderDTO createOrder2(Long customerId, OrderDTO orderDTO) {
        Long restaurantId = orderDTO.getRestaurantId();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        Order order = MapperUtil.toOrder(orderDTO);

        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setDeliveryGuy(getDeliveryGuy());

        order.setDeliveryAddress(orderDTO.getDeliveryAddress());

        List<OrderFoodItem> orderFoodItems = orderDTO.getOrderFoodItemDTOs().stream()
                .map(orderFoodItemDTO -> toOrderFoodItem(orderFoodItemDTO)).collect(Collectors.toList());

        Double totalPrice = 0.0;

        for (OrderFoodItem orderFoodItem : orderFoodItems) {
            totalPrice += orderFoodItem.getPrice()*orderFoodItem.getQuantity();
        }
        totalPrice += 25.0;
//        if (BigDecimal.valueOf(totalPrice).compareTo(BigDecimal.valueOf(orderDTO.getTotalPrice())) != 0) {
//            throw new PriceMismatchException("Total Price for this order should be " + totalPrice + " but found " + orderDTO.getTotalPrice());
//        }

        order.setTotalPrice(totalPrice);
        order.setOrderStatus(OrderStatus.APPROVED);
        orderRepository.save(order);
        orderFoodItems.forEach(orderFoodItem -> orderFoodItem.setOrder(order));
        orderFoodItemRepository.saveAll(orderFoodItems);
        return MapperUtil.toOrderDTO(order);
    }

    @Override
    public Double getRevenueAdmin(Integer month) {
        List<Order> orderList = orderRepository.getRevenueAdmin(month);
        Double revenue = 0.0;
        for(Order od : orderList) {
            revenue += od.getTotalPrice();
        }
        revenue = revenue*0.2;
        return revenue;
    }

    @Override
    public Double getRevenueRestaurant(Long restaurantId, Integer month) {
        List<Order> orderList = orderRepository.getRevenueRestaurant(restaurantId, month);
        Double revenue = 0.0;
        for(Order od : orderList) {
            revenue += od.getTotalPrice() - 25.0;
        }
        revenue = revenue*0.8;
        return revenue;
    }

    @Override
    public Double getRevenueDelivery(Long deliveryguyId, Integer month) {
        List<Order> orderList = orderRepository.getRevenueDelivery(deliveryguyId, month);
        Double revenue = 0.0;
        revenue = 20.0*orderList.size();
        return revenue;
    }


    private OrderFoodItem toOrderFoodItem(OrderFoodItemDTO orderFoodItemDTO) {
        log.debug("Converting orderFoodItemVo into orderFoodItem");
        OrderFoodItem orderFoodItem = new OrderFoodItem();
        Long foodItemId = orderFoodItemDTO.getFoodItemId();
        FoodItem foodItem = foodItemRepository.findById(foodItemId).orElseThrow(() -> new FoodItemNotFoundException(foodItemId));
        orderFoodItem.setFoodItem(foodItem);
        orderFoodItem.setQuantity(orderFoodItemDTO.getQuantity());
        orderFoodItem.setPrice(foodItem.getPrice());
//        Double totalPrice = orderFoodItemDTO.getQuantity() * foodItem.getPrice();
//        if (BigDecimal.valueOf(totalPrice).compareTo(BigDecimal.valueOf(orderFoodItemDTO.getPrice())) != 0) {
//            throw new PriceMismatchException("Total Price for " + foodItem.getName() + " should be " + totalPrice + " but found " + orderFoodItemDTO.getTotalPrice());
//        }
//        orderFoodItem.setTotalPrice(totalPrice);
        return orderFoodItem;
    }

    private DeliveryGuy getDeliveryGuy() {
        // TODO implement this in some non random way.
        // Should choose delivery who near restaurant or destination
        log.debug("Getting a delivery guy.");
//        List<DeliveryGuy> deliveryGuys = deliveryGuyRepository.findAll();
        List<DeliveryGuy> deliveryGuysNotBusy = deliveryGuyRepository.findAllNotBusy();
        Random random = new Random();
        DeliveryGuy deliveryGuy = deliveryGuysNotBusy.get(random.nextInt(deliveryGuysNotBusy.size()));
        deliveryGuy.setIsBusy(true);
        deliveryGuyRepository.save(deliveryGuy);
        return deliveryGuy;
    }
}