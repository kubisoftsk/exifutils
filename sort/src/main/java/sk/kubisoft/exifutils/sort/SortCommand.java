package sk.kubisoft.exifutils.sort;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SortCommand {

    @Inject
    public SortCommand() {
    }

    public void execute(SortCommandInput input) {
        System.out.println("Executing sort command with input: " + input);
    }

}
