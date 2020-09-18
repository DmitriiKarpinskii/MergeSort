import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Verification {
    private List<String> arguments;
    private Message message;

    Verification(String[] args) {
        arguments = new ArrayList<>(Arrays.asList(args));
        message = new Message(true, "ok");
    }

    Message check() {

        if (arguments.size() < 3) {
            message.setStatus(false);
            message.setMessage("Недостаточно аргументов для выполнения программы");
            return message;
        }
        if(!checkFirstArg()) return message;
        if(!checkSecondArg()) return message;
        if(!checkThird()) return message;
        checkFourthArgs();
        return message;

    }

    private boolean checkFirstArg() {
        switch (arguments.get(0)) {
            case "-a":
            case "-d":
                message.setStatus(true);
                break;
            case "-i":
            case "-s":
                arguments.add(0, "-a");
                message.setStatus(true);
                break;
            default:
                message.setStatus(false);
                message.setMessage("Недопустимый первый аргумент: >" + arguments.get(0) + "<");
                break;
        }
        return message.getStatus();
    }

    private boolean checkSecondArg() {
        switch (arguments.get(1)) {
            case "-i":
            case "-s":
                message.setStatus(true);
                break;
            default:
                message.setStatus(false);
                message.setMessage("Недопустимый второй аргумент: >" + arguments.get(1) + "<");
        }
        return message.getStatus();
    }

    private boolean checkThird() {
        String fileOutputName = arguments.get(2);
        File fileOutput = new File(fileOutputName);
        if (fileOutput.isFile()) {
            message.setStatus(true);
        } else {
            message.setStatus(false);
            message.setMessage("Файл(выходной):" + fileOutput.getAbsolutePath() + " не найден.");
        }
        return message.getStatus();
    }

    private boolean checkFourthArgs() {
        try {

            String fileOutputName = arguments.get(3);
            File fileOutput = new File(fileOutputName);
            if (fileOutput.isFile()) {
                message.setStatus(true);
            } else {
                message.setStatus(false);
                message.setMessage("Файл(входной):" + fileOutput.getAbsolutePath() + " не найден.");
            }
            return message.getStatus();
        } catch (IndexOutOfBoundsException e) {

            message.setStatus(false);
            message.setMessage("Не передан путь входного(ых) файла(ов)");
            return  message.getStatus();
        }

    }

    List<String> getArguments() {
        return arguments;
    }
}
