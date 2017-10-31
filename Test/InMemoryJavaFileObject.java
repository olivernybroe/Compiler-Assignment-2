import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

class InMemoryJavaFileObject extends SimpleJavaFileObject {
    private String contents = null;

    InMemoryJavaFileObject(String className, String contents) throws Exception {
        super(URI.create("string:///" + className.replace('.', '/') +
                Kind.SOURCE.extension), Kind.SOURCE);
        this.contents = contents;
    }

    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return contents;
    }
}