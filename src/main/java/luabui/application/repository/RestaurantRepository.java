package luabui.application.repository;

import luabui.application.model.Order;
import luabui.application.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("select res from Restaurant res where res.address like %:address%")
    Page<Restaurant> getRestaurantsByAddressLike(@Param("address")String address, Pageable pageable);

    @Query("select res from Restaurant res where res.createDate = :createDate")
    Page<Restaurant> getRestaurantsByDate(@Param("createDate")Date createDate, Pageable pageable);

    @Query("SELECT res FROM Restaurant res")
    Page<Restaurant> findAllInPage(Pageable pageable);

    Restaurant findByEmail(String email);

    @Query("select res from Restaurant res where res.name like %:name%")
    Page<Restaurant> getRestaurantsByName(@Param("name")String name, Pageable pageable);

    @Query("select res from Restaurant res where res.category.id = :category and res.isActive = true")
    Page<Restaurant> getRestaurantsByCategory(@Param("category")Long categoryId, Pageable pageable);

    @Query("select res from Restaurant res, FoodItem food where res.id = food.restaurant.id and food.name like %:name% group by res.id")
    Page<Restaurant> getRestaurantByFoodItemName(@Param("name")String name, Pageable pageable);

}
