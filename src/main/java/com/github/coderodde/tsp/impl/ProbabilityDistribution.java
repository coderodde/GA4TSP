package com.github.coderodde.tsp.impl;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
 
final class ProbabilityDistribution<E> {
 
    private static final class Entry<E> {
 
        private final E element;
        private double weight;
 
        Entry(E element, double weight) {
            this.element = element;
            this.weight = weight;
        }
 
        E getElement() {
            return element;
        }
 
        double getWeight() {
            return weight;
        }
 
        void setWeight(double weight) {
            this.weight = weight;
        }
    }
 
    private final List<Entry<E>> storage = new ArrayList<>();
    private final Map<E, Entry<E>> map = new HashMap<>();
    private Random random;
    private double totalWeight;
    
    public ProbabilityDistribution() {
        this(new Random());
    }
 
    public ProbabilityDistribution(Random random) {
        this.random = random;
    }
 
    public boolean addElement(E element, double weight) {
        Entry<E> entry = map.get(element);
 
        if (entry != null) {
            entry.setWeight(entry.getWeight() + weight);
        } else {
            entry = new Entry<>(element, weight);
            map.put(element, entry);
            storage.add(entry);
        }
 
        totalWeight += weight;
        return true;
    }
 
    public E sampleElement() {
        double value = random.nextDouble() * totalWeight;
        int distributionSize = storage.size();
 
        for (int i = 0; i < distributionSize; ++i) {
            Entry<E> entry = storage.get(i);
            double currentWeight = entry.getWeight();
 
            if (value < currentWeight) {
                return entry.getElement();
            }
 
            value -= currentWeight;
        }
 
        throw new IllegalStateException("Should not get here.");
    }
 
    public boolean removeElement(E element) {
        Entry<E> entry = map.remove(element);
 
        if (entry == null) {
            return false;
        }
 
        totalWeight -= entry.getWeight();
        storage.remove(entry);
        return true;
    }
 
    public void clear() {
        map.clear();
        storage.clear();
        totalWeight = 0.0;
    }
 
    public boolean isEmpty() {
        return storage.isEmpty();
    }
 
    public int size() {
        return storage.size();
    }
 
    public boolean contains(E element) {
        return map.containsKey(element);
    }
}