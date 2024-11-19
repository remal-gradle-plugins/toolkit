package name.remal.gradle_plugins.toolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.file.RelativePath;
import org.junit.jupiter.api.Test;

class RelativePathUtilsTest {

    private static final RelativePath RELATIVE_DIR_PATH = RelativePath.parse(false, "/dir");
    private static final RelativePath RELATIVE_FILE_PATH = RelativePath.parse(true, "/dir/file");

    @Test
    void getRelativePath() {
        assertEquals(
            RELATIVE_DIR_PATH,
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_DIR_PATH).getRelativePath(),
            RELATIVE_DIR_PATH.toString()
        );

        assertEquals(
            RELATIVE_FILE_PATH,
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_FILE_PATH).getRelativePath(),
            RELATIVE_FILE_PATH.toString()
        );
    }

    @Test
    void getPath() {
        assertEquals(
            RELATIVE_DIR_PATH.getPathString(),
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_DIR_PATH).getPath(),
            RELATIVE_DIR_PATH.toString()
        );

        assertEquals(
            RELATIVE_FILE_PATH.getPathString(),
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_FILE_PATH).getPath(),
            RELATIVE_FILE_PATH.toString()
        );
    }

    @Test
    void getName() {
        assertEquals(
            RELATIVE_DIR_PATH.getLastName(),
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_DIR_PATH).getName(),
            RELATIVE_DIR_PATH.toString()
        );

        assertEquals(
            RELATIVE_FILE_PATH.getLastName(),
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_FILE_PATH).getName(),
            RELATIVE_FILE_PATH.toString()
        );
    }

    @Test
    void isDirectory() {
        assertTrue(
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_DIR_PATH).isDirectory(),
            RELATIVE_DIR_PATH.toString()
        );

        assertFalse(
            RelativePathUtils.relativePathToMockedFileTreeElement(RELATIVE_FILE_PATH).isDirectory(),
            RELATIVE_FILE_PATH.toString()
        );
    }

}
