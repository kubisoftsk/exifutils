package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;
import sk.kubisoft.exifutils.core.media.MediaFile;
import sk.kubisoft.exifutils.core.media.MediaType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FileExplorerTest {

    @Mock
    private FileService fileService;
    @Mock
    private MediaTypeDetector mediaTypeDetector;

    private FileExplorer fileExplorer;

    @BeforeEach
    void setUp() throws IOException {
        fileExplorer = new FileExplorer(fileService, mediaTypeDetector);
        mockTestFiles();
    }

    private void mockTestFiles() throws IOException {
        List<Path> mockPaths = new ArrayList<>();
        Path root = Path.of("root");
        Path firstDir = root.resolve("first");

        addPath(mockPaths, root, true);
        addPath(mockPaths, root.resolve("IMG_00001.jpg"), false);
        addPath(mockPaths, root.resolve("IMG_00002.jpg"), false);
        addPath(mockPaths, root.resolve("DSC_4321.mov"), false);
        addPath(mockPaths, firstDir, true);
        addPath(mockPaths, firstDir.resolve("P2110001.jpg"), false);
        addPath(mockPaths, firstDir.resolve("P2110002.jpg"), false);
        addPath(mockPaths, firstDir.resolve("P2110003.mov"), false);
        addPath(mockPaths, firstDir.resolve("readme.txt"), false);

        lenient().when(fileService.walk(eq(root))).thenReturn(mockPaths.stream());
    }

    private void addPath(List<Path> allPaths, Path path, boolean isDirectory) {
        allPaths.add(path);

        lenient().when(fileService.isRegularFile(path)).thenReturn(!isDirectory);
        lenient().when(fileService.isDirectory(path)).thenReturn(isDirectory);

        lenient().when(fileService.isReadable(path)).thenReturn(true);
        lenient().when(fileService.exists(path)).thenReturn(true);

        if (path.getFileName().toString().endsWith(".jpg")) {
            lenient().when(mediaTypeDetector.detectMediaType(path)).thenReturn(MediaType.IMAGE);
        } else if (path.getFileName().toString().endsWith(".mov")) {
            lenient().when(mediaTypeDetector.detectMediaType(path)).thenReturn(MediaType.VIDEO);
        }
    }

    @Test
    void testListFilesSpecificPath() {
        var files = fileExplorer.listFiles(Path.of("root", "IMG_00001.jpg"));

        assertThat(files).containsExactlyInAnyOrder(Path.of("root", "IMG_00001.jpg"));
    }

    @Test
    void testListFilesSpecificFilePaths() {
        var files = fileExplorer.listFiles(List.of(
                Path.of("root", "IMG_00001.jpg"),
                Path.of("root", "first", "P2110001.jpg"))
        );

        assertThat(files).containsExactlyInAnyOrder(Path.of("root", "first", "P2110001.jpg"), Path.of("root", "IMG_00001.jpg"));
    }

    @Test
    void testListFilesRootDir() {
        var files = fileExplorer.listFiles(new String[]{"root"});

        assertThat(files).containsExactlyInAnyOrder(Path.of("root", "DSC_4321.mov"),
                Path.of("root", "first", "P2110001.jpg"),
                Path.of("root", "first", "P2110002.jpg"),
                Path.of("root", "first", "P2110003.mov"),
                Path.of("root", "first", "readme.txt"),
                Path.of("root", "IMG_00001.jpg"),
                Path.of("root", "IMG_00002.jpg")
        );
    }

    @Test
    void testListFilesNonExistentDir() {
        assertThatThrownBy(() -> fileExplorer.listFiles(new String[]{"nonexistent"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Source file / directory does not existnonexistent");
    }

    @Test
    void testListMediaFilesForRootDir() {
        var mediaFiles = fileExplorer.listMediaFiles(new String[]{"root"});

        assertThat(mediaFiles).hasSize(6);

        var paths = mediaFiles.stream().map(MediaFile::getOriginalPath).toList();

        assertThat(paths).containsExactlyInAnyOrder(Path.of("root", "DSC_4321.mov"),
                Path.of("root", "first", "P2110001.jpg"),
                Path.of("root", "first", "P2110002.jpg"),
                Path.of("root", "first", "P2110003.mov"),
                Path.of("root", "IMG_00001.jpg"),
                Path.of("root", "IMG_00002.jpg")
        );
    }

}
