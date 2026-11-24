package sk.kubisoft.exifutils.core.file;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.PatternSyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobPatternTest {

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            // case
            "*.jpg             ; IMG_0001.JPG      ; true",
            "*.JPG             ; IMG_0001.jpg      ; true",
            "img_*.Jpg         ; IMG_0001.jPG      ; true",
            "*.jpg             ; IMG_0001.jpeg     ; false",
            // * and **
            "*                 ; anything.xyz      ; true",
            "*                 ; .hidden           ; true",
            "IMG**.jpg         ; IMG_0001.jpg      ; true",
            "*-WA*             ; img-20240715-wa1  ; true",
            "IMG_*.jpg         ; IMG_.jpg          ; true",
            // ?
            "IMG_?.jpg         ; img_1.jpg         ; true",
            "IMG_?.jpg         ; IMG_.jpg          ; false",
            "IMG_?.jpg         ; IMG_12.jpg        ; false",
            "?.jpg             ; š.jpg             ; true",
            "?.jpg             ; 😀.jpg             ; true",
            // groups
            "*.{jpg,png}       ; a.JPG             ; true",
            "*.{jpg,png}       ; a.Png             ; true",
            "*.{jpg,png}       ; a.mov             ; false",
            "{IMG,VID}_*.{j*,m?v}; vid_1.MOV         ; true",
            "a{,b}.jpg         ; a.jpg             ; true",
            "a,b}.jpg          ; A,B}.jpg          ; true",
            // brackets
            "[abc].jpg         ; B.jpg             ; true",
            "[abc].jpg         ; d.jpg             ; false",
            "[a-c].jpg         ; B.jpg             ; true",
            "[A-C].jpg         ; b.jpg             ; true",
            "[a-c].jpg         ; D.jpg             ; false",
            "IMG_[0-35].jpg    ; img_2.jpg         ; true",
            "IMG_[0-35].jpg    ; img_5.jpg         ; true",
            "IMG_[0-35].jpg    ; img_4.jpg         ; false",
            "[!a].jpg          ; a.jpg             ; false",
            "[!a].jpg          ; A.jpg             ; false",
            "[!A].jpg          ; a.jpg             ; false",
            "[!a].jpg          ; b.jpg             ; true",
            "[!a-c].jpg        ; B.jpg             ; false",
            "[!a-c].jpg        ; D.jpg             ; true",
            "[-a].jpg          ; -.jpg             ; true",
            "[!-a].jpg         ; -.jpg             ; false",
            "[a-].jpg          ; -.jpg             ; true",
            "[*?].jpg          ; ?.jpg             ; true",
            "[*?].jpg          ; x.jpg             ; false",
            "[^a].jpg          ; ^.jpg             ; true",
            "[^a].jpg          ; b.jpg             ; false",
            "[a&&b].jpg        ; &.jpg             ; true",
            "[[].jpg           ; [.jpg             ; true",
            "a].jpg            ; A].jpg            ; true",
            // escapes
            "a\\*.jpg          ; A*.jpg            ; true",
            "a\\*.jpg          ; abc.jpg           ; false",
            "a\\?.jpg          ; ab.jpg            ; false",
            "\\[a\\].jpg       ; [A].jpg           ; true",
            "\\{a,b\\}.jpg     ; {A,B}.jpg         ; true",
            "\\\\a.jpg         ; \\A.jpg           ; true",
            "\\d.jpg           ; D.jpg             ; true",
            "\\d.jpg           ; 1.jpg             ; false",
            // regex metacharacters are literals
            "a.jpg             ; axjpg             ; false",
            "a(1)+$^|.jpg      ; A(1)+$^|.jpg      ; true",
            // diacritics
            "škôlka*           ; ŠKÔLKA.jpg        ; true",
            "*ČĎŤŇĽ*           ; x-čďťňľ.jpg       ; true",
            "[á-é].jpg         ; Á.jpg             ; true",
            "[!č].jpg          ; Č.jpg             ; false",
            "*.{ľ,š}           ; a.Š               ; true",
            "skolka*           ; škôlka.jpg        ; false",
    })
    void matches(String glob, String fileName, boolean expected) {
        assertThat(GlobPattern.compile(glob).matcher(fileName).matches()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"[", "[a", "[!", "[]", "[!]", "[c-a]", "[a-c-e]", "{a,b", "{a,{b}}", "a\\"})
    void rejectsInvalidGlob(String glob) {
        assertThatThrownBy(() -> GlobPattern.compile(glob)).isInstanceOf(PatternSyntaxException.class);
    }
}
