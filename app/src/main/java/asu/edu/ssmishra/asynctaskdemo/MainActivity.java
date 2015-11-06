package asu.edu.ssmishra.asynctaskdemo;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    EditText selectionText;
    ListView chooseImageList;
    String[] listOfImages;
    ProgressBar downloadImagesProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize all the components
        selectionText = (EditText) findViewById(R.id.imageUri);
        chooseImageList = (ListView) findViewById(R.id.listView);
        listOfImages = getResources().getStringArray(R.array.imageurls);
        downloadImagesProgress = (ProgressBar) findViewById(R.id.progressBar);

        chooseImageList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //change content of edittext
        selectionText.setText(listOfImages[position]);
    }

    public void downloadImage(View view){
        if(selectionText.getText().toString() != null && selectionText.getText().toString().length() > 0) {
            MyTask mytask = new MyTask();
            mytask.execute(selectionText.getText().toString());
        }
    }



    class MyTask extends AsyncTask<String, Integer, Boolean> {

        /*
         Params, the type of the parameters sent to the task upon execution.
         Progress, the type of the progress units published during the background computation.
         Result, the type of the result of the background computation.
        * */

        private int calculatedProgress = 0;
        private int contentlength = -1;
        private int counter = 0;
        @Override
        protected void onPreExecute() {
            downloadImagesProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean successful = false;
            URL downloadURL = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;

            File file = null;
            try{
                downloadURL = new URL(params[0]);
                connection = (HttpURLConnection) downloadURL.openConnection();
                contentlength = connection.getContentLength();   //Size of image
                inputStream = connection.getInputStream();

                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()+
                        "/"+ Uri.parse(params[0]).getLastPathSegment());
                fileOutputStream = new FileOutputStream(file);
                System.out.println("" + file.getAbsolutePath());
                int read = -1;
                byte[] buffer = new byte[1024];
                while((read = inputStream.read(buffer)) != -1){
                    fileOutputStream.write(buffer, 0, read);
                    counter = counter+read;                       //Number of bytes read
                    System.out.println(counter);
                    publishProgress(counter);                     // PublishProgress calls onProgressUpdate
                }
                successful = true;

            }catch (Exception e){

            }finally {
                if (connection != null){
                    connection.disconnect();
                }
                if(inputStream != null){
                    try{
                        inputStream.close();
                    }catch (IOException e){

                    }
                }
                if(fileOutputStream != null){
                    try{
                        fileOutputStream.close();
                    }catch (IOException e){

                    }
                }
            }
            return successful;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {   //Integer[] values
           calculatedProgress =  (int)(((double)values[0]/contentlength)*100);
           downloadImagesProgress.setProgress(calculatedProgress);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            downloadImagesProgress.setVisibility(View.GONE);
        }
    }
}

