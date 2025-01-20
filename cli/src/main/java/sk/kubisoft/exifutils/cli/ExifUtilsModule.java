package sk.kubisoft.exifutils.cli;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import sk.kubisoft.exifutils.cli.logging.SystemConsole;
import sk.kubisoft.exifutils.core.CommandRunner;
import sk.kubisoft.exifutils.core.logging.Console;
import sk.kubisoft.exifutils.dedupe.DeDupeCommandRunner;
import sk.kubisoft.exifutils.rename.RenameCommandRunner;
import sk.kubisoft.exifutils.setdate.SetDateCommandRunner;
import sk.kubisoft.exifutils.sort.SortCommandRunner;

import javax.inject.Singleton;
import java.util.Map;

@Module
public class ExifUtilsModule {

    @Provides
    @Singleton
    Console provideConsole() {
        return new SystemConsole();
    }

    @Provides
    @Singleton
    @IntoMap
    @StringKey("sort")
    CommandRunner provideSortCommandRunner(SortCommandRunner sortCommandRunner) {
        return sortCommandRunner;
    }

    @Provides
    @Singleton
    @IntoMap
    @StringKey("set-date")
    CommandRunner provideSetDateCommandRunner(SetDateCommandRunner setDateCommandRunner) {
        return setDateCommandRunner;
    }

    @Provides
    @Singleton
    @IntoMap
    @StringKey("dedupe")
    CommandRunner provideDeDupeCommandRunner(DeDupeCommandRunner deDupeCommandRunner) {
        return deDupeCommandRunner;
    }

    @Provides
    @Singleton
    @IntoMap
    @StringKey("rename")
    CommandRunner provideRenameCommandRunner(RenameCommandRunner renameCommandRunner) {
        return renameCommandRunner;
    }

    // Main component that will be used in the main method to bootstrap the application
    @Singleton
    @Component(modules = {ExifUtilsModule.class})
    public interface CliComponent {
        Map<String, CommandRunner> commandRunners();
        Console console();
    }
}
