package org.acme.getting.started.commandmode;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

@ApplicationScoped
public class ImageService
{
    private static final String IMAGE_DATA = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZMAAAC5CAYAAADtXNKFAAAQsUlEQVR4Xu3dW6hUZRsH8PfrvsygwAjKLgwis7Ik6KKSIAjTCkUi7CAkQdERIm86XQRCJ6sL6yKz0DA7IwQFlYEmSJkR4YVIRdCRDhfdSNn3PfMxw97j7Nkz+1lz2vN7YbMz13rWrN96nf+8611rzX+OHj36b9EIECBAgEBC4D/CJKFnVQIECBCoCQgTHYEAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2oAAECBAgIE32AAAECBNICwiRNqAABAgQICBN9gAABAgTSAsIkTagAAQIECAgTfYAAAQIE0gLCJE2owKAE/vjjj1L/+fPPP495Gf/++28544wzaj8aAQK9FRAmvfVVPSGwa9eu8s0339QC49tvv639d/0n/l837cQTTyznnXdeqf++9NJLy+mnny5oukG0LIE2AsJE9xiYQATD7t27y+HDh0uMIr744osZh8VMdyLC5bLLLqsFTQRM/GgECHQvIEy6N7PGDAU+/vjj8sknn5T4HT/D2iJYImAiWBYtWmT0MuAD9csvv5STTz65HDp0qBw5cqT8/PPPteOjDZeAMBmu4zHrXk2Exssvv1zefvvt2umqKtqcOXNqb/Axqqi3+p9jG/H/43eMfKLFfx84cGDGm47a8eYVwbJixQrhMmPJ7lZ89tlny5tvvlnidGerdtNNN5WHHnrI8eiOtWdLC5Oe0Y534QiPRx99tHbqqttWn8uoT57X/xwh0Rwi3dSOUInXEz8xBxO/p3qjalf3tNNOKxdeeGFt5CJcujkCnS27c+fO8sgjj5TPPvusoxUiUOJHG6yAMBms/6zbeoxE7r333o5CJEIiTiktXLiwLFmypMSbdPy53y1ec4xiIlgiYLodxdRPi914440Def399url9iJIli9f3vUmtm/fXlatWtX1elaoTkCYVGc51pXiU/8zzzxTnn766bans+KTfPzEaaNhvWS3PoKJcIkRVjfhEvsUn5JjHyeehhvrztHFzh933HHHLH311VeX+fPn10aTYVoP/uYFjVC6gO7BosKkB6jjVjL+ca9du3bKSfWY47j77rvLXXfdNZJvsPVwef7558v+/fvLTz/9VFrd1zLxuMeb3s0331wefPDBkdznfvfhGBFGH2o+LTrViCMm46+//vpJp8LmzZtX+zBjhNLvo/f/7QmTwbjPmq3GP96YG5lqcj0CJD4xzrZP6XFq7J133qmNXOIT81Qt9jv2PyaLZ5tBlZ34zDPPbFwwUa/74YcfTnvV1urVq8uOHTsaLyUukojA1/ovIEz6bz4rtjjdaCT+UW/evHks5hDCYsuWLbVPxVONWGJe5cUXXxwLj247+MqVK2tXbU1se/bsKRdffPG0pfbu3Vuuu+668uOPPzaWfffdd8uyZcumXdcC1QoIk2o9x6LadKOR+CQ+qqe0sgfwpZdeKhs3bpxynuXJJ5+snfLT/i/wwgsvlNtuu20SR1wSfPvtt3dMFH0t1plJGHW8EQtOKyBMpiWyQF1gust94+qsGI24oayUCJV77rmn5UglfOKTuNNe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8ODBli92WO8nGbzs+LwCYTI+x9qeEphWYPny5WXnzp3TLjdxAUHSFdesXViYzNpDa8cIzExg7969tdNeu3btalsgrv6K7weJkYtGQJjoAwQItBSIx41s27at7Nu3b9Lfx0hk2bJlZfHixeQINASEic5AgEBbge+//778/fff5ciRI2XBggW0CLQUECY6BgECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQECb6AAECBAikBYRJmlABAgQIEBAm+gABAgQIpAWESZpQAQIECBAQJvoAAQIECKQFhEmaUAECBAgQ+C9CtmAUEj4ccwAAAABJRU5ErkJggg==";
    private static final String IMAGE_OUTPUT_PATH = "target/img1.out.png";

    void resize()
    {
        System.out.println("AWT Graphics, is headless? " + GraphicsEnvironment.isHeadless());

        final InputStream imageStream = fromBase64(IMAGE_DATA);
        final InputStream imageResizedStream = resizeImage(imageStream, 100);
        save(imageResizedStream, IMAGE_OUTPUT_PATH);
    }

    private void save(InputStream inputStream, String savePath) {
        Path path = Paths.get(savePath);
        // create direcotry structure before saving file so that we dont get filentofoundexception
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            // in case direcotry already exists
        }
        // create file
        try {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Problem while saving file", e);
        }
    }

    private InputStream resizeImage(InputStream inputStream, int height)
    {
        BufferedImage inputImage = readImage(inputStream);

        int currentW = inputImage.getWidth();
        int currentH = inputImage.getHeight();
        int width = currentW * height / currentH;
        if (currentH < height)
        {
            width = currentW;
            height = currentH;
        }

        Image originalImage = inputImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        resizedImage.getGraphics().drawImage(originalImage, 0, 0, null);

        byte[] imageBytes = writeImage(resizedImage);
        return new ByteArrayInputStream(imageBytes);
    }

    private byte[] writeImage(BufferedImage resizedImage)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", outputStream);
            return outputStream.toByteArray();
        }
        catch (IOException io)
        {
            throw new UncheckedIOException("Cannot resize image, failed to write resized data to oputut stream", io);
        }
    }

    private BufferedImage readImage(InputStream inputStream)
    {
        try
        {
            return ImageIO.read(inputStream);
        }
        catch (IOException io)
        {
            throw new UncheckedIOException("Cannot resize image, failed to read image", io);
        }
    }

    private static InputStream fromBase64(final String content)
    {
        final String data = extractData(content);
        byte[] bytes = Base64.getDecoder().decode(data);
        return new ByteArrayInputStream(bytes);
    }

    private static String extractData(final String content)
    {
        if (content.startsWith("data:image"))
        {
            return content.split(",", 2)[1];
        }

        return content;
    }

}