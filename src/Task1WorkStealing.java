import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Task1WorkStealing {
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

        // Реалізація через Fork/Join Framework
        long startTime = System.nanoTime(); // Фіксуємо початковий час
        ForkJoinPool pool = new ForkJoinPool();
        SearchTask searchTask = new SearchTask(array, 0, array.length);
        Integer result = pool.invoke(searchTask);
        long endTime = System.nanoTime(); // Фіксуємо кінцевий час
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

    static class SearchTask extends RecursiveTask<Integer> {
        private final int[][] array;
        private final int startRow;
        private final int endRow;

        public SearchTask(int[][] array, int startRow, int endRow) {
            this.array = array;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected Integer compute() {
            if (endRow - startRow <= 10) { // Поріг розбиття
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < array[i].length; j++) {
                        if (array[i][j] == i + j) {
                            return array[i][j];
                        }
                    }
                }
                return null;
            } else {
                int mid = (startRow + endRow) / 2;
                SearchTask leftSearchTask = new SearchTask(array, startRow, mid);
                SearchTask rightSearchTask = new SearchTask(array, mid, endRow);

                leftSearchTask.fork();
                Integer rightResult = rightSearchTask.compute();
                Integer leftResult = leftSearchTask.join();

                return leftResult != null ? leftResult : rightResult;
            }
        }
    }
}
