package encryptor.util;


import encryptor.model.UserKey;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class KeyBaseDao {

    private static final String keyBaseDirPath = System.getProperty("user.home") + "/.simple-rijndael-encryptor/key-base";
    private static final String publicKeyFileName = "public.key";
    private static final String privateKeyFileName = "private.key";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ObservableSet<UserKey> keyBase = FXCollections.observableSet();

    public void refreshKeys() {
        final File baseDir = new File(keyBaseDirPath);
        if (!baseDir.exists())
            baseDir.mkdirs();

        File[] subDirs = baseDir.listFiles(File::isDirectory);

        keyBase.clear();
        for (File subDir : subDirs) {
            File publicKey = new File(subDir, publicKeyFileName);
            if (publicKey.exists()) {
                try {
                    keyBase.add(new UserKey(subDir.getName(), publicKey.getAbsolutePath()));
                } catch (Exception e) {
                    log.error("Failed to load key from" + subDir.getName(), e);
                }

            }

        }
    }

    public ObservableSet<UserKey> getKeys() {
        return FXCollections.unmodifiableObservableSet(keyBase);
    }

    public static String getPrivateKeyPath(String identifier) {
        return new File(new File(keyBaseDirPath, identifier), privateKeyFileName).getAbsolutePath();
    }


    public void removeKeys(ObservableList<UserKey> keys) {
        for (UserKey key : keys) {
            File dir = new File(keyBaseDirPath, key.getIdentifier());
            if (dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    log.error("File cannot be deleted", e);
                }
            }
        }
        keyBase.removeAll(keys);
    }

    public void addKey(UserKey userKey) {
        try {
            File baseDir = new File(keyBaseDirPath);
            File dir = new File(baseDir, userKey.getIdentifier());
            dir.mkdirs();
            File key = new File(dir, publicKeyFileName);
            RSAKeyFilesUtil.savePublicKey(userKey.getPublicKey(), key.getAbsolutePath());
            keyBase.add(userKey);
        } catch (Exception e) {
            log.error("Error occured durring key addition.", e);
            AlertUtil.showGenericError();
        }
    }
}
