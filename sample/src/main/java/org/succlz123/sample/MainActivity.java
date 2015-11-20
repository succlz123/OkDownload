package org.succlz123.sample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.succlz123.okdownload.OkDownloadManager;
import org.succlz123.okdownload.OkDownloadRequest;

import java.util.List;

/**
 * Created by succlz123 on 15/9/11.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv);

        DownloadRvAdapter adapter = new DownloadRvAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.query_all) {
            List<OkDownloadRequest> requestList = OkDownloadManager.getInstance(this).queryAll();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < requestList.size(); i++) {
                sb.append("(id=" + requestList.get(i).getId() + ")");
            }

            Toast.makeText(this, "found " + sb + " download records in the database", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.query_by_id) {
            final EditText editText = new EditText(this);

            new AlertDialog.Builder(this)
                    .setTitle("Please enter a query id")
                    .setView(editText, 20, 0, 20, 0)
                    .setPositiveButton("Determine", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String id = editText.getText().toString();
                            List<OkDownloadRequest> requestList = OkDownloadManager.getInstance(MainActivity.this).queryById(Integer.valueOf(id));
                            Toast.makeText(MainActivity.this, "found " + requestList.get(0).getUrl() + " download records in the database", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
