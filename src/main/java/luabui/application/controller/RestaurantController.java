package luabui.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import luabui.application.dto.*;
import luabui.application.service.CloudinaryService;
import luabui.application.service.FoodItemService;
import luabui.application.service.OrderService;
import luabui.application.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
/**
 * Controller to perform restaurant related operations.
 */
public class RestaurantController {
    private RestaurantService restaurantService;
    private FoodItemService foodItemService;
    private OrderService orderService;
    private CloudinaryService cloudinaryService;


    @Autowired
    public RestaurantController(RestaurantService restaurantService, FoodItemService foodItemService, OrderService orderService, CloudinaryService cloudinaryService) {
        this.restaurantService = restaurantService;
        this.foodItemService = foodItemService;
        this.orderService = orderService;
        this.cloudinaryService = cloudinaryService;
    }

    //Management Account

    @PostMapping(value = "/foodsun/signup/restaurants")
    public ResponseEntity<RestaurantDTO> saveRestaurant(@RequestParam("dto") String jsonFile, @RequestParam("file") MultipartFile file) {
        log.debug("Saving Restaurant.");
        String nameFile = cloudinaryService.uploadFile(file);
        RestaurantDTO restaurantDTO = null;
        try {
            restaurantDTO = new ObjectMapper().readValue(jsonFile, RestaurantDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        restaurantDTO.setAvatar(nameFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.save(restaurantDTO));
    }


    @GetMapping(value = "/restaurants/profile")
    public ResponseEntity<RestaurantDTO> getProfile(@RequestParam String email) {
        log.debug("Getting Restaurant by id.");
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.findByEmail(email));
    }

    @PutMapping(value = "restaurants/edit/{restaurantId}")
    public ResponseEntity<RestaurantDTO> updateProfile(@Valid @RequestBody RestaurantDTO restaurantDTO, @PathVariable Long restaurantId) {
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.update(restaurantDTO, restaurantId));
    }

    @PutMapping(value = "restaurants/edit/avatar/{restaurantId}")
    public ResponseEntity<RestaurantDTO> changeAvatar(@RequestParam("file") MultipartFile file, @PathVariable Long restaurantId) {
        String nameFile = cloudinaryService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.changeAvatar(nameFile, restaurantId));
    }

    /**
     * Returns restaurant belonging to given restaurantId.
     *
     * @param restaurantId
     * @return
     */
    @GetMapping(value = "/restaurants/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable Long restaurantId) {
        log.debug("Getting Restaurant by id.");
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.findById(restaurantId));
    }

    /**
     * Returns list of food items belonging to given restaurant.
     *
     * @param restaurantId
     * @return
     */
//    @GetMapping(value = "/restaurants/{restaurantId}/fooditems")
//    public ResponseEntity<Page<FoodItemDTO>> getRestaurantFoodItems(@PathVariable Long restaurantId) {
//        log.debug("Getting all FoodItems using restaurantId.");
//        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.getRestaurantFoodItems(restaurantId));
//    }

// Food Item Management
    /*
    * Get all food items by restaurantId
    * */
    @GetMapping(value = "/restaurants/{restaurantId}/fooditems")
    public ResponseEntity<Page<FoodItemDTO>> getFoodItemByRestaurantId(
                                                                    @PathVariable Long restaurantId,
                                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                   @RequestParam(value = "size", defaultValue = "3") Integer size) {
        PageRequest request = PageRequest.of(page - 1, size);
        Page<FoodItemDTO> list = restaurantService.getFoodItemByRestaurantId(restaurantId, request);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /*
     * Get food items by fooditemId
     * */
    @GetMapping(value = "/restaurants/fooditems/id/{fooditemId}")
    public ResponseEntity<FoodItemDTO> getFoodItemById(@PathVariable Long fooditemId) {
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.findFoodItemById(fooditemId));
    }

    /*
     * Get all food items by name
     * */
    @GetMapping(value = "/restaurants/{restaurantId}/fooditems/name/{name}")
    public ResponseEntity<Page<FoodItemDTO>> getFoodItemByNameAndResId(
                                                    @PathVariable Long restaurantId,
                                                    @PathVariable String name,
                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(value = "size", defaultValue = "3") Integer size) {
        PageRequest request = PageRequest.of(page - 1, size);
        Page<FoodItemDTO> list = restaurantService.getFoodItemByNameAndResId(restaurantId, name, request);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /*
     * Add one food item
     * */
    @PostMapping(value = "/restaurants/{restaurantId}/fooditems/add")
    public ResponseEntity<FoodItemDTO> addFoodItem(@PathVariable Long restaurantId, @RequestParam("dto") String jsonFile, @RequestParam("file") MultipartFile file) {
        String nameFile = cloudinaryService.uploadFile(file);
        FoodItemDTO foodItemDTO = null;
        try {
            foodItemDTO = new ObjectMapper().readValue(jsonFile, FoodItemDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        foodItemDTO.setImage(nameFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.addFoodItem(restaurantId, foodItemDTO));
    }

    /**
     * Persists list of food items provided in request body.
     *
     * @param restaurantId
     * @param foodItemDTOs
     * @return
     */
    @PostMapping(value = "/restaurants/{restaurantId}/fooditems")
    public ResponseEntity<RestaurantDTO> addFoodItems(@PathVariable Long restaurantId, @Valid @RequestBody List<FoodItemDTO> foodItemDTOs) {
        log.debug("Adding food items to restaurants.");
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.addFoodItems(restaurantId, foodItemDTOs));
    }

    /*
     * Edit foodItem
     * */
    @PutMapping(value = "/restaurants/fooditems/edit/{fooditemId}")
    public ResponseEntity<FoodItemDTO> editFoodItem(@PathVariable Long fooditemId, @Valid @RequestBody FoodItemDTO foodItemDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.editFoodItem(fooditemId, foodItemDTO));
    }

    @PutMapping(value = "/restaurants/fooditems/edit/image/{fooditemId}")
    public ResponseEntity<FoodItemDTO> changeImageFoodItem(@PathVariable Long fooditemId, @RequestParam("file") MultipartFile file) {
        String nameFile = cloudinaryService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.changeImageFoodItem(nameFile, fooditemId));
    }

    /*
     * delete foodItem
     * */
    @PutMapping(value = "/restaurants/fooditems/delete/{fooditemId}")
    public  ResponseEntity<Void> deleteFoodItem(@PathVariable Long fooditemId) {
        restaurantService.updateStatus(fooditemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping(value = "/restaurants/{restaurantId}/fooditems")
    public ResponseEntity<?> removeFoodItems(@PathVariable Long restaurantId, @Valid @RequestBody List<Long> foodItemIds) {
        log.debug("Removing food items from restaurants");
        /**
         * Delete food items when provided with food item ids.
         *
         * @param restaurantId
         * @param foodItemIds
         * @return
         */ restaurantService.removeFoodItems(restaurantId, foodItemIds);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns page of orders taken by restaurant.
     *
     * @param restaurantId
     * @return
     */
    @GetMapping(value = "/restaurants/{restaurantId}/orders")
    public ResponseEntity<Page<OrderDTO>> getOrdersByRestaurantId(@PathVariable Long restaurantId, @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                              @RequestParam(value = "size", defaultValue = "3") Integer size) {
        log.debug("Getting all Orders using restaurantId.");
        PageRequest request = PageRequest.of(page - 1, size);
        Page<OrderDTO> list = restaurantService.getOrdersByRestaurantId(restaurantId, request);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping(value = "/restaurants/{restaurantId}/orders/date/{date}")
    public ResponseEntity<Page<OrderDTO>> getOrdersByRestaurantIdAndDate(@PathVariable Long restaurantId, @PathVariable Date date,
                                                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                        @RequestParam(value = "size", defaultValue = "3") Integer size) {
        log.debug("Getting all Orders using restaurantId and Date.");
        PageRequest request = PageRequest.of(page - 1, size);
        Page<OrderDTO> list = restaurantService.getOrdersByRestaurantIdAndDate(restaurantId, date, request);
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    /**
     * Return a single order taken by restaurant.
     *
     * @param restaurantId
     * @param orderId
     * @return
     */
    @GetMapping(value = "/restaurants/{restaurantId}/orders/{orderId}")
    public ResponseEntity<OrderDTO> getRestaurantOrderById(@PathVariable Long restaurantId, @PathVariable Long orderId) {
        log.debug("Getting Restaurant Order By Restaurant Id.");
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.getRestaurantOrderById(restaurantId, orderId));
    }

    /**
     * Modify order status to preparing, cooking, cancelled.
     *
     *@param restaurantId
     * @param orderId
     * @param modification
     * @return
     */
    @PutMapping(value = "/restaurants/{restaurantId}/orders/{orderId}")
    public ResponseEntity<OrderDTO> modifyOrder(@PathVariable Long restaurantId, @PathVariable Long orderId, @Valid @RequestBody OrderModificationDTO modification) {
        log.debug("Modifying Customer Order.");
        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.modifyOrder(restaurantId, orderId, modification));
    }

    @GetMapping(value = "/restaurants/{restaurantId}/revenue/{month}")
    public ResponseEntity<Double> getRevenue(@PathVariable Long restaurantId, @PathVariable Integer month) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.getRevenueRestaurant(restaurantId, month));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.OK).body(cloudinaryService.uploadFile(file));
    }
//
//    @GetMapping("/statistic/{restaurantId}/{month}")
//    public ResponseEntity<Page<StatisticDTO>> getMostFoodItem(@PathVariable Long restaurantId, @PathVariable Integer month) {
//        PageRequest request = PageRequest.of(0, 3);
//        return ResponseEntity.status(HttpStatus.OK).body(restaurantService.getMostFoodItem(restaurantId, month, request));
//    }
}
