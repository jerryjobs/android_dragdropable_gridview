package cn.classd.demo.drag_drop_gridview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.classd.demo.drag_drop_gridview.bean.Book;
import cn.classd.dragablegrid.widget.DragableGridview;
import cn.classd.dragablegrid.widget.DragableGridview.OnItemClickListener;
import cn.classd.dragablegrid.widget.DragableGridview.OnSwappingListener;

/**
 * 
 * @author guojie  
 * jerry.jobs@qq.com
 *
 */
public class MainActivity extends Activity {
	
	public static final String TAG = "MainActivity";
	
	private List<Book> books;
	
	private DragableGridview mGridview;
	
	private  BookAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initTestData();
        
        mGridview = (DragableGridview) findViewById(R.id.dragableGridview1);
        
        adapter = new BookAdapter();
        mGridview.setAdapter(adapter);
        
        mGridview.setOnItemClick(new OnItemClickListener() {
			
			@Override
			public void click(int index) {
				Log.d(TAG, "item : " + index + " -- clicked!");
			}
		});
        
        mGridview.setOnSwappingListener(new OnSwappingListener() {
			
			@Override
			public void waspping(int oldIndex, int newIndex) {
				Book book = books.get(oldIndex);
				books.remove(oldIndex);
				books.add(newIndex, book);
				
				adapter.notifyDataSetChanged();
			}
		});
    }
    
    private class BookAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return books.size();
		}

		@Override
		public Object getItem(int position) {
			return books.get(position);
		}

		@Override
		public long getItemId(int position) {
			return books.get(position).getId();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				convertView = View.inflate(MainActivity.this, R.layout.item, null);
			}
			
			Book b = books.get(position);
			
			((ImageView) convertView.findViewById(R.id.imageView1)).setImageResource(b.getBitmapId());
			
			((TextView)convertView.findViewById(R.id.textView1)).setText(b.getName());
			
			return convertView;
		}
    	
    }
    
    private void initTestData() {
    	books = new ArrayList<Book>();
    	
		for (int i = 0; i < 1; i++) {
			setBooks();
		}
    }
    
    private void setBooks() {
    	
    	String[] bookNames = getResources().getStringArray(R.array.books);
    	
    	Book book = new Book();
    	book.setId(1);
    	book.setName(bookNames[0]);
    	book.setBitmapId(R.drawable.b001);
    	books.add(book);
    	
    	Book book1 = new Book();
    	book1.setId(2);
    	book1.setName(bookNames[1]);
    	book1.setBitmapId(R.drawable.b002);
    	books.add(book1);
    	
    	Book book2 = new Book();
    	book2.setId(3);
    	book2.setName(bookNames[2]);
    	book2.setBitmapId(R.drawable.b003);
    	books.add(book2);
    	
    	Book book3 = new Book();
    	book3.setId(4);
    	book3.setName(bookNames[3]);
    	book3.setBitmapId(R.drawable.b004);
    	books.add(book3);
    	
    	for (int i=0; i<10; i++) {
    		Book t = new Book();
        	t.setId(4);
        	t.setName(bookNames[3]);
        	t.setBitmapId(R.drawable.b004);
        	books.add(t);
    	}
    }
}
