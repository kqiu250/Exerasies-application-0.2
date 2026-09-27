package src;

public class Calorie_Calculator {
    private final int exerciseTime; //In Seconds
    private final int avgCalBurnRate; //In Calories per hour


    public Calorie_Calculator(int exerciseTime, int avgCalBurnRate) {
        this.exerciseTime = exerciseTime;
        this.avgCalBurnRate = avgCalBurnRate;
    }


    public int calcCaloriesBurnt () {
        return (int)(avgCalBurnRate * (exerciseTime / 3600.0));
    }
}
