# ProperBaseAdapter
Straightforward, fast, easy to use and adaptable generic RecyclerView adapter.\
Never create another RecyclerView adapter again.

## How does it work?
1. Define an AdapterItem representing an adapter item view type.
``` kotlin
class ImageViewRecyclerItem(private val drawable: Drawable?): AdapterItem<ImageView>() {
    // here we define how view is created
    override fun getNewView(parent: ViewGroup): ImageView {
        return ImageView(parent.context)
    }
    // here we populate the view
    override fun onViewBound(view: ImageView) {
        view.setImageDrawable(drawable)
        view.scaleType = ImageView.ScaleType.CENTER
    }
}
```
or inflate your own view.
``` kotlin
class TextRowRecyclerItem(private val text: String): AdapterItem<View>() {
    override fun getNewView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(android.R.layout.activity_list_item, parent, false)
    }
    override fun onViewBound(view: View) {
        view.findViewById<TextView>(R.id.text1).text = text
    }
}
```

AdapterItem subscribes to callbacks you can override to fine-tune behaviour in different states.
``` kotlin
override fun onItemViewAttached(view: YourView): Unit

override fun onItemViewDetached(view: YourView): Unit

override fun onItemViewRecycled(view: YourView): Unit

override fun onItemViewFailedToRecycle(view: YourView): Unit
```

and has a few public methods to help you manage it.
``` kotlin
fun getView(): YourView

fun setAnimation(@AnimRes animation: Int): Unit

fun setClickListener(clickListener: View.OnClickListener?): Unit

fun setMargins(startMargin: Int = 0, topMargin: Int = 0, endMargin: Int = 0, bottomMargin: Int = 0): Unit

fun setViewTag(viewTag: Any?): Unit
```

<br/>2. Have your Activity, Fragment or View implement BaseRecyclerViewImplementation .
``` kotlin
class MainActivity : AppCompatActivity(), ProperBaseAdapterImplementation {
  ...
  // library needs to know how to locate a RecyclerView
  override fun getRecyclerView(): RecyclerView? {
        return findViewById(R.id.recyclerView)
  }

  // add items to provided data list, those will be added to the adapter.
  override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
    data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.btn_radio))
        .withMargins(topMargin = resources.getDimensionPixelSize(R.dimen.dp16))
                
    for (i in 1..10) {
        data.add(TextViewRecyclerItem("Text item $i")
            .withMargins(
                startMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                topMargin = resources.getDimensionPixelSize(R.dimen.dp8),
                endMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                bottomMargin = resources.getDimensionPixelSize(R.dimen.dp8))
            .withAnimation(android.R.anim.fade_in)
            .withClickListener(View.OnClickListener {
                Toast.makeText(this, "Clicked item $i", Toast.LENGTH_SHORT).show()
            }))
    }
    
    data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now)))

    return data
  }
  ...
```

<br/>3. Simply calling refreshRecyclerView method will do the rest and populate recycler view with provided data.
``` kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    refreshRecyclerView()
}
```

<br/>You're of course not required use a provided BaseRecyclerViewImplementation and simply construct ProperBaseAdapter yourself and set it to your RecyclerView.\
<br/>Make use of multiple adapter's public methods based on your needs:
``` kotlin
fun addItems(dataObjects: List<AdapterItem<*>>?, notifyItemRangeChanged: Boolean = true): Unit

fun getItemAt(position: Int): AdapterItem<*>?

fun getItemByViewTag(viewTag: Any): AdapterItem<*>?

fun getItemTypeAt(position: Int): KClass<*>

fun getPositionForItemWithViewTag(viewTag: Any): Int?

fun notifyItemWithViewTagChanged(viewTag: Any): Unit

fun updateItems(newItems: List<AdapterItem<*>>): Unit

fun removeAllItems(notifyDataSetChanged: Boolean = true): Unit

fun removeItems(fromPosition: Int, itemCount: Int = 1, notifyDataSetChanged: Boolean = true): Unit

fun setItems(newData: MutableList<AdapterItem<*>>, notifyDataSetChanged: Boolean = true): Unit
```

## Nice! How do I get started?
Add it in your root build.gradle at the end of repositories:
``` gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

And make sure this is in your app build.gradle
``` gradle
dependencies {
  implementation 'com.github.mvojtkovszky:ProperBaseAdapter:1.1.0'
}
```
