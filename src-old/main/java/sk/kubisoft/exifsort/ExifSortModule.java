package sk.kubisoft.exifsort;

import dagger.Module;
import dagger.Provides;
import sk.kubisoft.exifsort.config.ConfigService;
import sk.kubisoft.exifsort.config.MediaFileSorter;

import javax.inject.Singleton;

@Module
public class ExifSortModule {

    @Provides
    @Singleton
    ConfigService provideConfigService() {
        return ConfigService.getInstance();
    }

    @Provides
    @Singleton
    FileExplorer provideFileExplorer() {
        return new FileExplorer();
    }

    @Provides
    @Singleton
    MediaDateExtractor provideMediaDateExtractor() {
        return new MediaDateExtractor();
    }

    @Provides
    @Singleton
    MediaFileSorter provideMediaFileSorter() {
        return new MediaFileSorter();
    }

    @Provides
    @Singleton
    FileMover provideFileMover() {
        return new FileMover();
    }

    @Provides
    @Singleton
    DuplicateFileHandler provideDuplicateFileHandler() {
        return new DuplicateFileHandler();
    }

    @Provides
    @Singleton
    ExifSort provideExifSort(
            FileExplorer fileExplorer,
            MediaDateExtractor mediaDateExtractor,
            MediaFileSorter mediaFileSorter,
            FileMover fileMover) {
        return new ExifSort(fileExplorer, mediaDateExtractor, mediaFileSorter, fileMover);
    }
}
