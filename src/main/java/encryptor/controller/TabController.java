package encryptor.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by Wojtek on 2015-03-07.
 */
public class TabController {

    protected StringProperty inputFilePathProperty = new SimpleStringProperty();
    protected StringProperty outputFilePathProperty = new SimpleStringProperty();

    public void bindInputFile(StringProperty pathProperty) {
        inputFilePathProperty.bindBidirectional(pathProperty);
    }
    public void bindOutputFile(StringProperty pathProperty) {
        outputFilePathProperty.bindBidirectional(pathProperty);
    }
}
