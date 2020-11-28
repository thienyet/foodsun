package luabui.application.service;



import luabui.application.dto.*;

import java.util.List;

public interface RestaurantService extends CrudService<RestaurantDTO, Long> {
    RestaurantDTO addFoodItems(Long restaurantId, List<FoodItemDTO> foodItemDTOs);

    RestaurantDTO removeFoodItems(Long restaurantId, List<Long> foodItemIds);

    List<OrderDTO> getRestaurantOrders(Long restaurantId);

    OrderDTO getRestaurantOrderById(Long restaurantId, Long orderId);

    OrderDTO modifyOrder(Long restaurantId, Long orderId, OrderModificationDTO modification);

    List<FoodItemDTO> getRestaurantFoodItems(Long restaurantId);

    List<RestaurantDTO> findRestaurantByAddressLike(String address);

    RestaurantDTO update(RestaurantDTO restaurantDTO, Long restaurantId);
}
