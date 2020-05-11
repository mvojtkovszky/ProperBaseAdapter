# ProperBaseAdapter
Straightforward, fast, easy to use and adaptable generic RecyclerView adapter.

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

Or inflate your own view.
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

AdapterItem has other methods you can override to fine-tune behaviour based on the adapter's callbacks.
``` kotlin
override fun onItemViewAttached(view: YourView) { }
override fun onItemViewDetached(view: YourView) { }
override fun onItemViewRecycled(view: YourView) { }
override fun onItemViewFailedToRecycle(view: YourView) { }
```

2. Have your Activity or Fragment implement ProperBaseAdapterImplementation .
``` kotlin
class MainActivity : AppCompatActivity(), ProperBaseAdapterImplementation {
  ...
  // library needs to know how to locate a RecyclerView
  override fun getRecyclerView(): RecyclerView? {
        return findViewById(R.id.recyclerView)
  }

  // add items to provided data list, those will be added to the adapter.
  override fun getAdapterData(data: MutableList<AdapterItem<*>>): MutableList<AdapterItem<*>> {
    // let's say we want to display an image on top
    data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.btn_radio))
        .withMargins(topMargin = resources.getDimensionPixelSize(R.dimen.dp16))
    // and add 10 text view items
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
    // and add another image on bottom
    data.add(ImageViewRecyclerItem(ContextCompat.getDrawable(this, android.R.drawable.ic_btn_speak_now)))

    return data
  }
  ...
```

3. Simply calling refreshRecyclerView method will do the rest and populate RecyclerView with provided data.
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
  implementation 'com.github.mvojtkovszky:ProperBaseAdapter:1.1.0'
}
```
