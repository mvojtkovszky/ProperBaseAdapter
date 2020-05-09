# ProperBaseAdapter
Straightforward, fast, easy to use and adaptable generic RecyclerView adapter.
You'll never have to create another adapter or define types of adapter items again

## How do I get started

1. Define a bunch of Adapter view items by extending AdapterItem class
``` kotlin
class ImageViewRecyclerItem(private val drawable: Drawable?): AdapterItem<ImageView>() {
    // here we define how view is created
    override fun getNewView(parent: ViewGroup): ImageView {
        return ImageView(parent.context)
    }

    // here we define how view will look like
    override fun onViewBound(view: ImageView) {
        view.setImageDrawable(drawable)
        view.scaleType = ImageView.ScaleType.CENTER
    }
}
```

or inflate your own view and do whatever you please with it
``` kotlin
class RowRecyclerItem(private val text: String): AdapterItem<View>() {
    override fun getNewView(parent: ViewGroup): View {
        return LayoutInflater.from(parent.context).inflate(android.R.layout.activity_list_item, parent, false)
    }

    override fun onViewBound(view: View) {
        view.findViewById<TextView>(R.id.text1).text = text
    }
}
```

there are a bunch of other methods you can override to fine-tune behaviour of your adapter items
``` kotlin
    override fun onItemViewAttached(view: View) { }
    override fun onItemViewDetached(view: View) { }
    override fun onItemViewRecycled(view: View) { }
    override fun onFailedToRecycleView(view: View) { }
```

2. Have your Activity or Fragment implement BaseRecyclerViewImplementation, link a RecyclerView and throw in a dataset at will 
``` kotlin
class MainActivity : AppCompatActivity(), BaseRecyclerViewImplementation {
  ...
  override fun getRecyclerView(): RecyclerView? {
        return findViewById(R.id.recyclerView)
  }
    
  override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
    data.add(
        ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.btn_radio))
            .withMargins(
                topMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                endMargin = resources.getDimensionPixelSize(R.dimen.dp16)))
                
    for (i in 1..10) {
        data.add(
            TextViewRecyclerItem("Text item $i")
                .withMargins(
                    startMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                    topMargin = resources.getDimensionPixelSize(R.dimen.dp8),
                    endMargin = resources.getDimensionPixelSize(R.dimen.dp16),
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.dp8))
                //.withAnimation(android.R.anim.fade_in)
                .withClickListener(View.OnClickListener {
                    Toast.makeText(this, "Clicked item $i", Toast.LENGTH_SHORT).show()
                }))
    }
    
    data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now)))

    return data
  }
  ...
```

3. Simply calling refreshRecyclerView method will do the rest, for example right when the app starts
``` kotlin
override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        refreshRecyclerView()
    }
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
  implementation 'com.github.mvojtkovszky:ProperBaseAdapter:1.0.0'
}
```
