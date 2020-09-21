import com.sun.xml.internal.ws.api.model.wsdl.WSDLPortType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MergeArrays {

    public static void main(String[] args) {

        Verification verification = new Verification(args);
        Message message = verification.check(); //проверка на корректность переданных аргументов

        if (message.getStatus()) { // Если проверка пройдена , начинается выполнение программы.
            // Если нет, выбрасыватся сообщение с информацией об ошибки.

            String[] argsForVerification = new String[verification.getArguments().size()];
            argsForVerification = verification.getArguments().toArray(argsForVerification);
            String order = argsForVerification[0];
            String type = argsForVerification[1];
            String fileNameOut = argsForVerification[2];
            String[] fileNames = Arrays.copyOfRange(argsForVerification, 3, argsForVerification.length);

            List<BoxStream> streams = new LinkedList<>();
            List<String> peekValues = new LinkedList<>();

            HashMap<BoxStream,String> mapStreams = new HashMap<>();

            //Открываем для каждого входного файла поток и добавляем его в лист.
            for (String fileName : fileNames) {
                File file = new File(fileName);

                try {
                    if (Files.size(file.toPath()) != 0) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        BoxStream boxStream = new BoxStream(reader);
                        streams.add(boxStream);
                        peekValues.add("");

                        mapStreams.put(boxStream,""); // **********
                    }
                } catch (FileNotFoundException e) {
                    System.out.println(new Message(false, "Файл (входной) не найден. " + fileName));
                } catch (IOException e) {
                    System.out.println(new Message(false, "Ошибка при получении размера файла. " + fileName));
                }
            }

            BoxStream currentStream;
            boolean validValue = false;

            while (true) {
                //После открытия потоков берем из листа по очереди каждый поток и считываем из него строку.
                // for(Map.Entry streams : mapStreams.entrySet()) **********************************
                for (int i = 0; i < streams.size(); i++) {
                    System.out.println("==================== Текущий стрим " + (i + 1) + "====================");

                    //
                    currentStream = streams.get(i);

                    try {
                        System.out.println("Готов? - " + currentStream.getReader().ready() + " Читаем следующий элемент? - " + currentStream.isGetNext());
                        if (currentStream.getReader().ready() && currentStream.isGetNext()) {
                            currentStream.read();
                            validValue = checkValidValue(currentStream.getCurrentValue(), currentStream.getLastValue(), type);
                            System.out.println(currentStream.getLastValue() + " >> " + currentStream.getCurrentValue());
                            if (validValue) {
                                System.out.println("Добавляем в лист: " + currentStream.getCurrentValue());
                                peekValues.set(i, currentStream.getCurrentValue());
                            } else {
                                currentStream.close();
                                peekValues.remove(i);
                                streams.remove(i);
                                i--;
                            }
                        }

                    } catch (IOException e) {
                        System.out.println(new Message(false, "Ошибка при чтении входного файла"));
                    }
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileNameOut, true))) {

                    String min = findMin(peekValues, type); // находим минимальное значение из считанных
                    String value;

                    System.out.println("Пиковые значения после прохода стримов:  " + peekValues);
                    for (int i = 0; i < peekValues.size(); i++) {


                        System.out.println(i + " - size : " + peekValues.size());
                        value = peekValues.get(i);

                        if (value.equals(min)) {

                            System.out.println("Пишем мин. в файл " + value);
                            writer.write(value); // записываем минимальное значение в результирующий файл
                            writer.newLine();
                            writer.flush();

                            if (!streams.get(i).getReader().ready() || !validValue) {
                                peekValues.remove(i);
                                streams.remove(i);
                                i--;
                                continue;
                            }

                            streams.get(i).setGetNext(true);
                        } else
                            streams.get(i).setGetNext(false);
                    }

                    if (streams.size() == 0) {
                        break;
                    }


                } catch (IOException e) {
                    System.out.println(new Message(false, "Ошибка записи в результирующий файл"));
                    System.exit(0);
                }
            }

            if (order.equals("-d")) { //если передан аргумент -d , то дополнительно сортируем выходной файл по убыванию.

                descending(fileNameOut);
            }
            System.out.println("Программа выполнена.");
        } else {
            System.out.println(message);
            System.exit(0);
        }
    }


    private static String findMin(List<String> list, String type) {

        switch (type) {
            case "-i": {
                Integer minInt = Integer.parseInt(list.get(0));
                Integer x;
                for (int i = 1; i < list.size(); i++) {
                    x = Integer.parseInt(list.get(i));
                    if (minInt.compareTo(x) > 0) {
                        minInt = x;
                    }
                }
                return String.valueOf(minInt);
            }
            case "-s": {

                String minString = list.get(0);
                String s;
                for (int i = 1; i < list.size(); i++) {
                    s = list.get(i);
                    if (minString.compareTo(s) > 0) {
                        minString = s;
                    }
                }
                return minString;
            }

            default:
                return null;

        }
    }

    private static boolean checkValidValue(String currentValue, String lastValue, String type) {
        boolean isValid = true;
        switch (type) {
            case "-i": {
                try {
                    Integer cValue = Integer.parseInt(currentValue);
                    Integer lValue = Integer.parseInt(lastValue);
                    isValid = cValue.compareTo(lValue) >= 0;

                } catch (NumberFormatException e) {
                    isValid = false;
                    System.out.println("Ошибка при преобразовании значения. Строка >" + currentValue + "< содержит для [Integer] недопусимые символы.");
                    e.printStackTrace();
                }
            }
            break;
            case "-s": {

                if (currentValue.compareTo(lastValue) < 0) {
                    isValid = false;
                    System.out.println("Нарушен порядок сортировки. Строка >" + currentValue + "< лексеграфически меньше значения " + lastValue);

                }

                if (currentValue.contains(" ")) {
                    isValid = false;
                    System.out.println("Ошибка при проверки данных : Считаная строка: >" + currentValue + "< содержит пробелы");
                }
            }
            break;

            default:
                isValid = false;
                break;
        }


        return isValid;
    }

    private static void descending(String fileNameIn) {

        Path pathDir = Paths.get("");

        Stack<Path> stack = new Stack<>();
        List<String> bufferStr = new ArrayList<>();

        Path pathFileIn = Paths.get(fileNameIn);


        try {
            Path dir = Files.createTempDirectory(pathDir, "tmp-");
            Path pathTmpOut = Files.createTempFile(pathFileIn.getParent(), "tmp-", ".tmp");


            try (FileReader fr = new FileReader(fileNameIn);
                 BufferedReader reader = new BufferedReader(fr)

            ) {
                while (reader.ready()) {

                    while (reader.ready() && bufferStr.size() < 10000) {
                        String line = reader.readLine();
                        bufferStr.add(line);
                    }

                    Path tempFile = Files.createTempFile(dir, "tmp-", ".tmp");


                    try (
                            FileWriter fileWrTemp = new FileWriter(tempFile.toAbsolutePath().toString());
                            BufferedWriter writerTmpFiles = new BufferedWriter(fileWrTemp)
                    ) {

                        while (bufferStr.size() != 0) {

                            writerTmpFiles.write(bufferStr.remove(bufferStr.size() - 1));
                            writerTmpFiles.newLine();
                        }
                        stack.push(tempFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileOutputStream fos = new FileOutputStream(pathTmpOut.toAbsolutePath().toString());
                 BufferedOutputStream bos = new BufferedOutputStream(fos)
            ) {
                while (!stack.empty()) {

                    Path path = stack.pop();
                    try (FileInputStream is = new FileInputStream(path.toAbsolutePath().toString());
                         BufferedInputStream bis = new BufferedInputStream(is)) {

                        byte[] buffer = new byte[bis.available()];
                        bis.read(buffer);
                        bos.write(buffer);
                        bis.close();
                        Files.delete(path);
                    } catch (IOException e) {

                    }

                }
                Files.delete(dir);

            } catch (IOException e) {
                System.out.println(new Message(false, "Ошибка записи/чтения временого файла"));
            }

            Files.move(pathTmpOut, pathFileIn, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            System.out.println("Ошибка во время создания временного файла");
            e.printStackTrace();
            System.exit(0);
        }
    }

}
