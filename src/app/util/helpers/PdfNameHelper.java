package app.util.helpers;

import app.model.invoice.InvoiceModel;
import app.model.item.ClientItem;

import java.util.List;

/**
 * Created on 11/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class PdfNameHelper {

    public static String suggestFileName(InvoiceModel invoice) {
        String clientName = findFirstClientName(invoice.getItems());
        if (clientName == null) {
            clientName = invoice.getDate().toString();
        }

        return invoice.getId() + "_" + clientName;
    }

    private static String findFirstClientName(List<ClientItem> items) {
        for (ClientItem item : items) {
            if (!item.getClientName().isEmpty()) {
                String[] names = item.getClientName().split(" ");
                return names[names.length - 1];
            }
        }

        return null;
    }


//        String fileNamePattern = Config.sharedPreferences().getProperty(SharedProperty.PDF_FILE_NAME);
//
//        String wordPattern = "(?:\\[([0-9]+)\\])?";
//        String clientPattern = "\\$client\\[([0-9]+)\\]\\.([a-z]+)" + wordPattern;
//        Pattern p = Pattern.compile(clientPattern);
//        Matcher matcher = p.matcher(fileNamePattern);
//
//
//        while (matcher.find()) {
//            String replacement;
//
//            int clientIndex = Integer.parseInt(matcher.group(1));
//            String field = matcher.group(2);
//            if (clientIndex >= invoice.getItems().size()) {
//                replacement = "";
//            } else {
//                String value = getClientField(invoice.getItems().get(clientIndex), field);
//                replacement = getWord(value, matcher.group(3));
//            }
//
//            fileNamePattern = matcher.replaceFirst(replacement);
//            matcher = p.matcher(fileNamePattern);
//        }
//
//        fileNamePattern = fileNamePattern
//                .replaceAll("\\$id", InvoiceHelper.idFormat().format(invoice.getId()))
//                .replaceAll("\\$date", new DateConverter("dd-MM-yyyy").toString(invoice.getDate()));
//
//
//        p = Pattern.compile("address" + wordPattern);
//        matcher = p.matcher(fileNamePattern);
//        while (matcher.find()) {
//            String replacement = getWord(invoice.getAddress(), matcher.group(1));
//            fileNamePattern = matcher.replaceFirst(replacement);
//            matcher = p.matcher(fileNamePattern);
//        }
//
//        return fileNamePattern;
//    }
//
//    private static String getWord(String value, String wordIndex) {
//        if (wordIndex == null) {
//            return value;
//        }
//
//        int index = Integer.parseInt(wordIndex);
//        String[] words = value.replaceAll("\n", " ").split(" ");
//        if (index >= words.length) {
//            return value;
//        }
//
//        return words[index];
//    }
//
//    private static String getClientField(ClientItem client, String field) {
//        switch (field) {
//            case "name":
//                return client.getClientName();
////            case "item":
////                return client.getItems().get(0).getItem().itemNameProperty();
////            case "fromdate":
////            case "date":
////                return new SimpleDateFormat("dd-MM-yyyy").format(client.getFromDate());
////            case "todate":
////                return new SimpleDateFormat("dd-MM-yyyy").format(client.getToDate());
////            case "price":
////                return new DecimalFormat("#0.00").format(client.getPrice());
//            default:
//                throw new IllegalArgumentException("Unknown field: " + field);
//        }
//    }

}
