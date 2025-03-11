package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sk.kubisoft.exifutils.core.analysis.MediaTypeDetector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        addPath(mockPaths, firstDir.resolve("P2110002.mov"), false);

        lenient().when(fileService.walk(eq(root))).thenReturn(mockPaths.stream());
    }

    private void addPath(List<Path> allPaths, Path path, boolean isDirectory) {
        allPaths.add(path);

        lenient().when(fileService.isRegularFile(path)).thenReturn(!isDirectory);
        lenient().when(fileService.isDirectory(path)).thenReturn(isDirectory);

        lenient().when(fileService.isReadable(path)).thenReturn(true);
        lenient().when(fileService.exists(path)).thenReturn(true);
    }

    @Test
    void testListFiles() {
        var args = new String[]{"root"};

        var files = fileExplorer.listFiles(args);

        assertThat(files).hasSize(7);
    }

}
