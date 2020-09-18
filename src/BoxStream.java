import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

class BoxStream {
    private BufferedReader reader;
    private String currentValue;
    private String lastValue;
    private boolean readNext;

    BoxStream(BufferedReader reader) {
        this.reader = reader;
        this.readNext = true;
    }

    void read() {
        try {
            if (currentValue == null)
                lastValue = currentValue = reader.readLine();
            else {
                lastValue = currentValue;
                currentValue = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении входного файла");
            currentValue = lastValue;
        }
    }

    void close() {

        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии потока. ");
            e.printStackTrace();
        }
    }

    Reader getReader() {
        return reader;
    }

    String getCurrentValue() {
        return currentValue;
    }

    String getLastValue() {
        return lastValue;
    }

    boolean isGetNext() {
        return readNext;
    }

    void setGetNext(boolean getNext) {
        this.readNext = getNext;
    }
}

