package com.techwhizer.snsbiosystem;

import com.victorlaerte.asynctask.AsyncTask;

import java.util.HashMap;
import java.util.Map;

public class TaskSample {

    private void executeTask(){
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.setDaemon(false);
        myAsyncTask.execute();


    }
    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        public void onPreExecute() {

        }

        @Override
        public Boolean doInBackground(String... params) {

            return false;

        }

        @Override
        public void onPostExecute(Boolean success) {
        }

        @Override
        public void progressCallback(Integer... params) {

        }
    }
}
