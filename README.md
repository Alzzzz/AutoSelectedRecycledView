# AutoSelectedRecycledView
   
## 主要功能
   默认选中第一个View的RecyclerView
   大概效果如同app store和google store左右滑动内容
   
## 主要实现
   实现很简单，主要利用SnapHelper，例如LinearSnapHelper（默认选中中间位置）。

## 用法
   AutoSelectStartHelper mAutoSelectStartHelper = new AutoSelectStartHelper();
   mAutoSelectStartHelper.attachToRecyclerView(recyclerView);
