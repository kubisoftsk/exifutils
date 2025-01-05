package sk.kubisoft.exifsort;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = ExifSortModule.class)
public interface ExifSortComponent {
    ExifSort getExifSort();

    static ExifSortComponent create() {
        return DaggerExifSortComponent.create();
    }
}
