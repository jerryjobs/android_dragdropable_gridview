android_dragdropable_gridview
=============================


* implement gridview drag and drop
* use long press to drag
* implement onclick event


------------------------
##for example

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

##bugs

* drag with an scrolled gridview
* selected  item animations
* drop down to grid animation to tip


 
