package models.recipe;

import java.util.ArrayList;
import java.util.List;
import models.item.Dish;
import models.item.Preparable;
import models.enums.IngredientState;
import models.ingredients.Dough;
import models.ingredients.Tomato;
import models.ingredients.Cheese;
import models.ingredients.Sausage;
import models.ingredients.Chicken;

public class RecipeTest {
    
    public static void main(String[] args) {
        System.out.println("=== Recipe Test Suite ===\n");
        
        testRecipeFactory();
        testRecipeIngredientRequirement();
        testPizzaMargheritaRecipe();
        testPizzaSosisRecipe();
        testPizzaAyamRecipe();
        testDishMatching();
        testMissingComponents();
        
        System.out.println("\n=== All tests completed ===");
    }
    
    public static void testRecipeFactory() {
        System.out.println("Testing PizzaRecipeFactory...");
        
        PizzaRecipeFactory factory = new PizzaRecipeFactory();
        List<Recipe> recipes = factory.createAllRecipes();
        
        // Test that all recipes are created
        assert recipes.size() == 3 : "Should have 3 recipes";
        System.out.println("✓ Created " + recipes.size() + " recipes");
        
        // Test getRecipeByName
        Recipe margherita = factory.getRecipeByName("Pizza Margherita");
        assert margherita != null : "Should find Pizza Margherita";
        assert margherita.getId().equals("PIZZA_MARGHERITA") : "Correct ID";
        System.out.println("✓ Found recipe by name: " + margherita.getName());
        
        // Test getRecipeById
        Recipe sosis = factory.getRecipeById("PIZZA_SOSIS");
        assert sosis != null : "Should find Pizza Sosis";
        assert sosis.getName().equals("Pizza Sosis") : "Correct name";
        System.out.println("✓ Found recipe by ID: " + sosis.getId());
        
        // Test non-existent recipe
        Recipe nonExistent = factory.getRecipeByName("Pizza Impossible");
        assert nonExistent == null : "Should return null for non-existent recipe";
        System.out.println("✓ Correctly handled non-existent recipe");
        
        System.out.println("PizzaRecipeFactory tests passed!\n");
    }
    
    public static void testRecipeIngredientRequirement() {
        System.out.println("Testing RecipeIngredientRequirement...");
        
        RecipeIngredientRequirement req = new RecipeIngredientRequirement(
            Dough.class, IngredientState.CHOPPED, 1
        );
        
        // Test properties
        assert req.getIngredientType() == Dough.class : "Correct ingredient type";
        assert req.getRequiredState() == IngredientState.CHOPPED : "Correct state";
        assert req.getQuantity() == 1 : "Correct quantity";
        System.out.println("✓ Properties set correctly");
        
        // Test matching with correct ingredient
        Dough dough = new Dough();
        dough.chop(); // Change state to CHOPPED
        assert req.matches(dough) : "Should match chopped dough";
        System.out.println("✓ Correctly matches valid ingredient");
        
        // Test matching with wrong state
        Dough rawDough = new Dough(); // RAW state
        assert !req.matches(rawDough) : "Should not match raw dough";
        System.out.println("✓ Correctly rejects wrong state");
        
        // Test matching with wrong ingredient type
        Tomato tomato = new Tomato();
        tomato.chop();
        assert !req.matches(tomato) : "Should not match different ingredient";
        System.out.println("✓ Correctly rejects wrong ingredient type");
        
        System.out.println("RecipeIngredientRequirement tests passed!\n");
    }
    
    public static void testPizzaMargheritaRecipe() {
        System.out.println("Testing PizzaMargheritaRecipe...");
        
        Recipe recipe = new PizzaMargheritaRecipe();
        
        // Test basic properties
        assert recipe.getId().equals("PIZZA_MARGHERITA") : "Correct ID";
        assert recipe.getName().equals("Pizza Margherita") : "Correct name";
        assert recipe.getBaseReward() == 120 : "Correct reward";
        assert recipe.getBasePenalty() == -50 : "Correct penalty";
        assert recipe.getBakeTimeSeconds() == 12 : "Correct bake time";
        assert recipe.isPlateRequired() == true : "Plate required";
        System.out.println("✓ Basic properties correct");
        
        // Test requirements
        List<RecipeIngredientRequirement> requirements = recipe.getRequiredComponents();
        assert requirements.size() == 3 : "Should have 3 requirements";
        System.out.println("✓ Has correct number of requirements: " + requirements.size());
        
        // Test satisfaction with correct ingredients
        List<Preparable> correctIngredients = new ArrayList<>();
        Dough dough = new Dough();
        dough.chop();
        Tomato tomato = new Tomato();
        tomato.chop();
        Cheese cheese = new Cheese();
        cheese.chop();
        
        correctIngredients.add(dough);
        correctIngredients.add(tomato);
        correctIngredients.add(cheese);
        
        assert recipe.isSatisfiedBy(correctIngredients) : "Should be satisfied by correct ingredients";
        System.out.println("✓ Satisfied by correct ingredients");
        
        // Test with missing ingredient
        List<Preparable> incompleteIngredients = new ArrayList<>();
        incompleteIngredients.add(dough);
        incompleteIngredients.add(tomato);
        // Missing cheese
        
        assert !recipe.isSatisfiedBy(incompleteIngredients) : "Should not be satisfied by incomplete ingredients";
        System.out.println("✓ Not satisfied by incomplete ingredients");
        
        System.out.println("PizzaMargheritaRecipe tests passed!\n");
    }
    
    public static void testPizzaSosisRecipe() {
        System.out.println("Testing PizzaSosisRecipe...");
        
        Recipe recipe = new PizzaSosisRecipe();
        
        // Test basic properties
        assert recipe.getId().equals("PIZZA_SOSIS") : "Correct ID";
        assert recipe.getName().equals("Pizza Sosis") : "Correct name";
        assert recipe.getBaseReward() == 150 : "Correct reward";
        assert recipe.getBakeTimeSeconds() == 14 : "Correct bake time";
        System.out.println("✓ Basic properties correct");
        
        // Test requirements (should have 4: dough, tomato, cheese, sausage)
        List<RecipeIngredientRequirement> requirements = recipe.getRequiredComponents();
        assert requirements.size() == 4 : "Should have 4 requirements";
        System.out.println("✓ Has correct number of requirements: " + requirements.size());
        
        // Test satisfaction with correct ingredients
        List<Preparable> correctIngredients = new ArrayList<>();
        Dough dough = new Dough();
        dough.chop();
        Tomato tomato = new Tomato();
        tomato.chop();
        Cheese cheese = new Cheese();
        cheese.chop();
        Sausage sausage = new Sausage();
        sausage.chop();
        
        correctIngredients.add(dough);
        correctIngredients.add(tomato);
        correctIngredients.add(cheese);
        correctIngredients.add(sausage);
        
        assert recipe.isSatisfiedBy(correctIngredients) : "Should be satisfied by correct ingredients";
        System.out.println("✓ Satisfied by correct ingredients");
        
        System.out.println("PizzaSosisRecipe tests passed!\n");
    }
    
    public static void testPizzaAyamRecipe() {
        System.out.println("Testing PizzaAyamRecipe...");
        
        Recipe recipe = new PizzaAyamRecipe();
        
        // Test basic properties
        assert recipe.getId().equals("PIZZA_AYAM") : "Correct ID";
        assert recipe.getName().equals("Pizza Ayam") : "Correct name";
        assert recipe.getBaseReward() == 160 : "Correct reward";
        assert recipe.getBakeTimeSeconds() == 15 : "Correct bake time";
        System.out.println("✓ Basic properties correct");
        
        // Test requirements (should have 4: dough, tomato, cheese, chicken)
        List<RecipeIngredientRequirement> requirements = recipe.getRequiredComponents();
        assert requirements.size() == 4 : "Should have 4 requirements";
        System.out.println("✓ Has correct number of requirements: " + requirements.size());
        
        // Test satisfaction with correct ingredients
        List<Preparable> correctIngredients = new ArrayList<>();
        Dough dough = new Dough();
        dough.chop();
        Tomato tomato = new Tomato();
        tomato.chop();
        Cheese cheese = new Cheese();
        cheese.chop();
        Chicken chicken = new Chicken();
        chicken.chop();
        
        correctIngredients.add(dough);
        correctIngredients.add(tomato);
        correctIngredients.add(cheese);
        correctIngredients.add(chicken);
        
        assert recipe.isSatisfiedBy(correctIngredients) : "Should be satisfied by correct ingredients";
        System.out.println("✓ Satisfied by correct ingredients");
        
        System.out.println("PizzaAyamRecipe tests passed!\n");
    }
    
    public static void testDishMatching() {
        System.out.println("Testing Dish matching...");
        
        Recipe recipe = new PizzaMargheritaRecipe();
        
        // Create a dish with correct components
        Dish dish = new Dish("Test Pizza");
        Dough dough = new Dough();
        dough.chop();
        Tomato tomato = new Tomato();
        tomato.chop();
        Cheese cheese = new Cheese();
        cheese.chop();
        
        dish.addComponent(dough);
        dish.addComponent(tomato);
        dish.addComponent(cheese);
        
        assert recipe.matchesDish(dish) : "Recipe should match correct dish";
        System.out.println("✓ Recipe matches correct dish");
        
        // Create incomplete dish
        Dish incompleteDish = new Dish("Incomplete Pizza");
        incompleteDish.addComponent(dough);
        incompleteDish.addComponent(tomato);
        // Missing cheese
        
        assert !recipe.matchesDish(incompleteDish) : "Recipe should not match incomplete dish";
        System.out.println("✓ Recipe correctly rejects incomplete dish");
        
        System.out.println("Dish matching tests passed!\n");
    }
    
    public static void testMissingComponents() {
        System.out.println("Testing missing components detection...");
        
        Recipe recipe = new PizzaSosisRecipe();
        
        // Create partial ingredients list (missing sausage)
        List<Preparable> partialIngredients = new ArrayList<>();
        Dough dough = new Dough();
        dough.chop();
        Tomato tomato = new Tomato();
        tomato.chop();
        Cheese cheese = new Cheese();
        cheese.chop();
        
        partialIngredients.add(dough);
        partialIngredients.add(tomato);
        partialIngredients.add(cheese);
        
        List<RecipeIngredientRequirement> missing = recipe.missingComponents(partialIngredients);
        assert missing.size() == 1 : "Should have 1 missing component";
        assert missing.get(0).getIngredientType() == Sausage.class : "Missing component should be Sausage";
        System.out.println("✓ Correctly identified missing component: " + missing.get(0).getIngredientType().getSimpleName());
        
        // Test with complete ingredients
        Sausage sausage = new Sausage();
        sausage.chop();
        partialIngredients.add(sausage);
        
        List<RecipeIngredientRequirement> noMissing = recipe.missingComponents(partialIngredients);
        assert noMissing.size() == 0 : "Should have no missing components";
        System.out.println("✓ Correctly identified complete ingredient set");
        
        System.out.println("Missing components tests passed!\n");
    }
}