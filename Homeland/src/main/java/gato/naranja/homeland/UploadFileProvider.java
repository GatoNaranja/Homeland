package gato.naranja.homeland;


import androidx.core.content.FileProvider;

/**
 * Android 7.0 禁止在应用外部公开 file:// URI，所以我们必须使用 content:// 替代
 */
public class UploadFileProvider extends FileProvider {

}
