module GraphicsModule {
	
	requires java.base;
	requires javafx.base;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	exports LegoScorer;
	
}