package org.main;

import java.util.*;

public class InputData {
    protected static List<Item> items = Arrays.asList(
            new Item("Candy"),
            new Item("Drink"),
            new Item("Soda"),
            new Item("Popcorn"),
            new Item("Snacks")
    );

    public static Map<User, HashMap<Item, Double>> initializeData(int numberOfUsers) {
        Map<User, HashMap<Item, Double>> data = new HashMap<>();
        HashMap<Item, Double> newUser;
        Set<Item> newRecommendationSet;
        for (int i = 0; i < numberOfUsers; i++) {
            newUser = new HashMap<>();
            newRecommendationSet = new HashSet<>();
            for (int j = 0; j < 3; j++) {
                newRecommendationSet.add(items.get((int) (Math.random() * 5)));
            }
            for (Item item : newRecommendationSet) {
                newUser.put(item, Math.random());
            }
            data.put(new User("User " + i), newUser);
        }
        return data;
    }
}
