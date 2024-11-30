import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.List;

public class ImageFinder {
    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {

        File directory = new File("Folder");

        // Пошук зображень за WorkStealing
        ForkJoinPool pool = new ForkJoinPool();
        ImageSearchTask task = new ImageSearchTask(directory);
        List<File> images = pool.invoke(task);

        if (images.isEmpty()) {
            System.out.println("Зображення не знайдено.");
        } else {
            System.out.println("Кількість знайдених зображень: " + images.size());
            File lastImage = images.get(images.size() - 1);
            System.out.println("Відкривається файл: " + lastImage.getAbsolutePath());
            Desktop.getDesktop().open(lastImage);
        }
    }

    static class ImageSearchTask extends RecursiveTask<List<File>> {
        private final File directory;

        public ImageSearchTask(File directory) {
            this.directory = directory;
        }

        @Override
        protected List<File> compute() {
            List<File> images = new CopyOnWriteArrayList<>();
            File[] files = directory.listFiles();

            if (files == null) return images;

            List<ImageSearchTask> tasks = new CopyOnWriteArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    ImageSearchTask task = new ImageSearchTask(file);
                    tasks.add(task);
                    task.fork();
                } else if (isImageFile(file)) {
                    images.add(file);
                }
            }

            for (ImageSearchTask task : tasks) {
                images.addAll(task.join());
            }

            return images;
        }

        private boolean isImageFile(File file) {
            String name = file.getName().toLowerCase();
            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
        }
    }
}
