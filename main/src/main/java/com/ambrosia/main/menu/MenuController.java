package com.ambrosia.main.menu;

import com.ambrosia.main.aws.service.StorageService;
import com.ambrosia.main.menu.entity.Item;
import com.ambrosia.main.menu.entity.ItemCategory;
import com.ambrosia.main.menu.model.AddItemRequestDTO;
import com.ambrosia.main.menu.service.ItemCategoryService;
import com.ambrosia.main.menu.service.ItemService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {
    private final StorageService storageService;
    private final ItemService itemService;
    private final ItemCategoryService itemCategoryService;

    @Autowired
    public MenuController(StorageService storageService, ItemService itemService, ItemCategoryService itemCategoryService) {
        this.storageService = storageService;
        this.itemService = itemService;
        this.itemCategoryService = itemCategoryService;
    }

    @PostMapping("/add-item")
    public ResponseEntity<?> addNewItem(
            @RequestParam(value = "file")MultipartFile file,
            @RequestParam(value = "item") String jsonItem) {
        try {
            AddItemRequestDTO addItemRequest = new ObjectMapper().readValue(jsonItem, AddItemRequestDTO.class);
            String imageFileName = storageService.uploadFile(file); // storing to AWS and getting the image file name
            if(itemService.addNewMenuItem(addItemRequest, imageFileName) != null)
                return ResponseEntity.ok("Item successfully added");
            return ResponseEntity.badRequest().body("Failed to store the item in the database");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(400).body("Failed to transform item string to json");
        }
    }

    @PostMapping("/add-new-category")
    public ResponseEntity<?> addNewItemCategory(@RequestParam("category") String categoryName) {
        return  ResponseEntity.ok().body(itemCategoryService.createNewCategory(categoryName));
    }

    @GetMapping("/get-all-items")
    public ResponseEntity<?> getAllItems() {
        return ResponseEntity.ok().body(itemService.getAllItems());
    }

    @GetMapping("/get-items-by-category/{category}")
    public ResponseEntity<?> getItemsByCategory(@PathVariable String category) {
        List<Item> items = itemService.getAllItemsByCategory(category);
        if(items == null) {
            return ResponseEntity.status(404).body("Category not found");
        }
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/get-all-item-categories")
    public ResponseEntity<?> getAllItemCategories() {
        List<ItemCategory> categories = itemCategoryService.getAllItemCategories();
        return ResponseEntity.ok().body(categories);
    }

    @PutMapping("/modify-item")
    public ResponseEntity<?> modifyItem(@RequestBody Item item) {
        System.out.println(item);
        return ResponseEntity.ok().body(itemService.modifyItem(item));
    }

    @PutMapping("/modify-item-image")
    public ResponseEntity<?> modifyImage(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        // while updating an item, the category must be present in the database
        // no new category will be created
        return ResponseEntity.ok().body(itemService.modifyImage(file, id));
    }

    @PutMapping("/rename-item-category")
    public ResponseEntity<?> renameItemCategory(@RequestParam("newName") String newName, @RequestParam("oldName") String oldName){
        ItemCategory itemCategory = itemCategoryService.renameCategory(newName, oldName);
        if(itemCategory == null) {
            return ResponseEntity.status(404).body("Item category not found");
        }
        return ResponseEntity.ok().body(itemCategory);
    }

    @DeleteMapping("/delete-item")
    public ResponseEntity<?> deleteItem(@RequestParam("id") Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok("Item deleted successfully");
    }
}
