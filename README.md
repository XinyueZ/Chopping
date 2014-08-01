Chopping
========

Config App with a remote link. When network doesn't work good, the fallback will work for the App. 

Based on Google Volley, Otto-Bus.

Integrate into projects created by Android Studio:

1. git submodule add https://github.com/XinyueZ/Chopping.git submodules/ChoppingBootstrap
2. To get main code of Chopping run: git submodule update -i
3. cd submodules/ChoppingBootstrap
4. To get updated Google Volley: git submodule update -i
5. Switch To top level of project and edit settings.gradle, add:

	include ':app', ':ChoppingBootstrap', ':volley_cz'
	project(':ChoppingBootstrap').projectDir = new File('submodules/ChoppingBootstrap')
	project(':volley_cz').projectDir = new File('submodules/ChoppingBootstrap/submodules/volley_cz')
 
6. Switch to app/build.gradle. Add or edit dependencies {} like:

    compile project(':ChoppingBootstrap')
    compile project(':volley_cz')

Example loading App's configuration(From https://github.com/XinyueZ/ExampleChopping):
	
    main
	|____java
	      |__com.app  
    |____resources
          |__app.properties  
          |__fallback.properties(other names are ok.)
          
    **In app.properties:**
    app_config=https://dl.dropboxusercontent.com/s/tk28mec9h47vjlc/choppingexample.properties
    app_config_fallback=fallback.properties
    
    **In fallback.properties** 
    It should use same info that has been saved behind 'app_config'.
    
    **In Java:**
	App class:
    public final class App extends Application {

      @Override
      public void onCreate() {
          super.onCreate();
          init();
  
      }
  
      private void init() {
          //Init SharePefrerence.
          Prefs.createInstance(this);
          //Init Volley.
          TaskHelper.init(getApplicationContext());
      }
    }
    
    Activity class:
    @Subscribe
    public void onApplicationConfigurationDownloaded(ApplicationConfigurationDownloadedEvent _e) {
		TextView textView = (TextView) findViewById(R.id.output_tv);
		textView.setText(Prefs.getInstance().getOneProperty());
	}

	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main); 
		String mightError = null;
		try {
			Prefs.getInstance().downloadApplicationConfiguration();
		} catch (InvalidAppPropertiesException _e) {
			mightError = _e.getMessage();
		} catch (CanNotOpenOrFindAppPropertiesException _e) {
			mightError = _e.getMessage();
		}
		if (mightError != null) {
			new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(mightError).setCancelable(false)
					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).create().show();
		}
	}

	@Override
	protected void onResume() {
		BusProvider.getBus().register(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		BusProvider.getBus().unregister(this);
		super.onPause();
	}
