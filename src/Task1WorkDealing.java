import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Task1WorkDealing {

    private static final int THREADS = 3;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Отримання параметрів
        System.out.print("Введіть кількість рядків: ");
        int rows = scanner.nextInt();

        System.out.print("Введіть кількість стовпців: ");
        int cols = scanner.nextInt();

        System.out.print("Введіть мінімальне значення елементів масиву: ");
        int min = scanner.nextInt();

        System.out.print("Введіть максимальне значення елементів масиву: ");
        int max = scanner.nextInt();

        if (min > max || rows <= 0 || cols <= 0) {
            System.out.println("Некоректні дані. Спробуйте ще раз.");
            return;
        }

        // Генерація масиву
        int[][] array = generateArray(rows, cols, min, max);
        printArray(array);

        // Реалізація через Work Dealing
        long startTime = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<Integer>> futures = new ArrayList<>();

        // Розподіл задач по потоках
        for (int i = 0; i < rows; i++) {
            futures.add(executor.submit(new SearchCallable(array, i)));
        }

        Integer result = null;
        try {
            for (Future<Integer> future : futures) {
                Integer rowResult = future.get();
                if (rowResult != null) {
                    result = rowResult;
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Помилка виконання: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        long endTime = System.nanoTime();
        if (result != null) {
            System.out.println("Знайдений елемент: " + result);
        } else {
            System.out.println("Елемент не знайдено.");
        }
        System.out.println("Час виконання (мс): " + (endTime - startTime) / 1_000_000);
    }

    private static int[][] generateArray(int rows, int cols, int min, int max) {
        Random random = new Random();
        int[][] array = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = random.nextInt(max - min + 1) + min;
            }
        }
        return array;
    }

    private static void printArray(int[][] array) {
        for (int[] row : array) {
            for (int value : row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    static class SearchCallable implements Callable<Integer> {
        private final int[][] array;
        private final int row;

        public SearchCallable(int[][] array, int row) {
            this.array = array;
            this.row = row;
        }

        @Override
        public Integer call() {
            for (int j = 0; j < array[row].length; j++) {
                if (array[row][j] == row + j) {
                    return array[row][j];
                }
            }
            return null;
        }
    }
}
