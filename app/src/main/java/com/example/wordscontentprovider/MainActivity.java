package com.example.wordscontentprovider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ContentResolver resolver;
    Uri newUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resolver= this.getContentResolver();
        //为ListView注册上下文菜单
        ListView list = (ListView) findViewById(R.id.lstWords);
        registerForContextMenu(list);

        //在列表显示全部单词
        ArrayList<Map<String, String>> items=getAll();
        setWordsListView(items);

    }

    private void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId,R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.lstWords);

        list.setAdapter(adapter);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.contextmenu_wordslistview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView textId=null;
        TextView textWord=null;
        TextView textMeaning=null;
        TextView textSample=null;

        AdapterView.AdapterContextMenuInfo info=null;
        View itemView=null;

        switch (item.getItemId()){
            case R.id.delete:
                //删除单词
                textId =(TextView)findViewById(R.id.textId);
                if(textId!=null){
                    String strId=textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.upgrade:
                //修改单词
                textId =(TextView)findViewById(R.id.textId);
                textWord =(TextView)findViewById(R.id.textViewWord);
                textMeaning =(TextView)findViewById(R.id.textViewMeaning);
                textSample =(TextView)findViewById(R.id.textViewSample);
                if(textId!=null && textWord!=null && textMeaning!=null && textSample!=null){
                    String strId=textId.getText().toString();
                    String strWord=textWord.getText().toString();
                    String strMeaning=textMeaning.getText().toString();
                    String strSample=textSample.getText().toString();
                    UpdateDialog(strId);
                }
                break;
        }
        return true;
    }

    private void UpdateDialog(final String strId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View viewDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.upgradedialog, null, false);
        builder.setTitle("更新单词").setView(viewDialog)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText EditWord=(EditText)viewDialog.findViewById(R.id.EditWord);
                        EditText EditMeaning=(EditText)viewDialog.findViewById(R.id.EditMeaning);
                        EditText EditSample=(EditText) viewDialog.findViewById(R.id.EditSample);
                        ContentValues values = new ContentValues();
                        values.put(Words.Word.COLUMN_NAME_WORD, EditWord.getText().toString());
                        values.put(Words.Word.COLUMN_NAME_MEANING, EditMeaning.getText().toString());
                        values.put(Words.Word.COLUMN_NAME_SAMPLE, EditSample.getText().toString());

                        Uri uri= Uri.parse(Words.Word.CONTENT_URI_STRING);
                        resolver.update(uri,values,null,new String[]{strId});
                        //单词已经更新，更新显示列表
                        ArrayList<Map<String, String>> items=getAll();
                        setWordsListView(items);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    private void DeleteDialog(final String strId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View viewDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.deletedialog, null, false);
        builder.setTitle("删除单词").setView(viewDialog)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING );
                        resolver.delete(uri, null , new  String []{strId});
                        ArrayList<Map<String, String>> items=getAll();
                        setWordsListView(items);
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.search:
                //查找
                SearchDialog();
                return true;
            case R.id.insert:
                //新增单词
                InsertDialog();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void InsertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View viewDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.adddialog, null, false);
        builder.setTitle("添加单词").setView(viewDialog)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText word=(EditText) viewDialog.findViewById(R.id.addWord);
                        EditText meaning=(EditText) viewDialog.findViewById(R.id.addMeaning);
                        EditText sample=(EditText) viewDialog.findViewById(R.id.addSample);
                        ContentValues values = new ContentValues();
                        values.put(Words.Word.COLUMN_NAME_WORD, word.getText().toString());
                        values.put(Words.Word.COLUMN_NAME_MEANING, meaning.getText().toString());
                        values.put(Words.Word.COLUMN_NAME_SAMPLE, sample.getText().toString());
                        newUri = resolver.insert(Words.Word.CONTENT_URI, values);
                        ArrayList<Map<String, String>> items=getAll();
                        setWordsListView(items);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();
    }

    private void SearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View viewDialog = LayoutInflater.from(MainActivity.this).inflate(R.layout.searchdialog, null, false);
        builder.setTitle("查找单词").setView(viewDialog)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING );
                        Cursor cursor = resolver.query(Words.Word.CONTENT_URI,
                                new String[] { Words.Word._ID, Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING,Words.Word.COLUMN_NAME_SAMPLE},
                                Words.Word._ID+"=?", new String []{"3"}, null);
                        ArrayList<Map<String, String>> item=ConvertCursor2List( cursor);
                        setWordsListView(item);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create().show();

    }

    private ArrayList<Map<String, String>> getAll() {


        String[] projection = {
                Words.Word._ID,
                Words.Word.COLUMN_NAME_WORD,
                Words.Word.COLUMN_NAME_MEANING,
                Words.Word.COLUMN_NAME_SAMPLE
        };

        //排序
        String sortOrder =
                Words.Word.COLUMN_NAME_WORD + " DESC";

        Cursor c=resolver.query(Uri.parse(Words.Word.CONTENT_URI_STRING),projection,null,null,sortOrder);


        return ConvertCursor2List(c);
    }

    private ArrayList<Map<String, String>> ConvertCursor2List(Cursor cursor) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        if(cursor!=null){
        while (cursor.moveToNext()) {
            Map<String, String> map = new HashMap<>();
            map.put(Words.Word._ID, String.valueOf(cursor.getInt(0)));
            map.put(Words.Word.COLUMN_NAME_WORD, cursor.getString(1));
            map.put(Words.Word.COLUMN_NAME_MEANING, cursor.getString(2));
            map.put(Words.Word.COLUMN_NAME_SAMPLE, cursor.getString(3));
            result.add(map);
        }
            return result;
        }
        else {
            Toast.makeText(MainActivity.this,"没有找到数据",Toast.LENGTH_LONG).show();
               return result;
        }

    }
}