package com.modex.modex.view;

import com.modex.modex.datastruct.Edge;
import com.modex.modex.datastruct.Parcel;
import com.modex.modex.datastruct.Province;
import com.modex.modex.mechanic.GameController;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIControl extends Application {

    private GameController gameController;

    private final Translate mapTranslate = new Translate(0, 0);
    private final Scale mapScale = new Scale(1, 1, 0, 0);
    private final double midX = (97.34 + 105.65) / 2;
    private final double midY = (5.61 + 20.46) / 2;
    private final double MAP_SCALE = 50.0;


    private boolean isZoomed = false;
    private final double GRID_SIZE = 20.0;

    private Rotate hourRotate;
    private Rotate minuteRotate;

    private VBox timeMenu;
    private boolean isMenuOpen = false;
    private Group mapGroup;

    private boolean isDrawProvinces = false;

    private Map<Province, ImageView> nodeSprites = new HashMap<>();
    private Map<String, Line> edgeLines = new HashMap<>();

    private Image lockedImage;
    private Image unlockedImage;
    private Image startNodeImage;
    private Image constructionImage;
    private Image startbtnImage;
    private Image logo;
    private final double SPRITE_SIZE = 8.0;

    private Label moneyLabel;

    private AnchorPane clockPane;
    private Line hourHand;
    private Line minuteHand;

    private StackPane root;
    private StackPane mainGameContent;
    private StackPane modalOverlay;
    private AnchorPane uiLayer;

    private HBox conveyor_slot;
    private AnchorPane conveyor_tile;
    private Label inbound_label;

    private final java.util.List<Node> conveyorQueue = new java.util.ArrayList<>();
    private StackPane[] truckSlots = new StackPane[5];
    private AnchorPane truckMenu;
    private Image deliveryBtnImage;

    private StackPane startOverlay;

    private StackPane summaryOverlay;

    private Image[] TruckImages = new Image[7];

    @Override
    public void start(Stage stage) throws IOException {
        try {
            lockedImage = new Image(getClass().getResourceAsStream("/images/node_locked.png"));
            unlockedImage = new Image(getClass().getResourceAsStream("/images/node_unlocked.png"));
            startNodeImage = new Image(getClass().getResourceAsStream("/images/node_start.png"));
            constructionImage = new Image(getClass().getResourceAsStream("/images/node_construction.png"));

            startbtnImage = new Image(getClass().getResourceAsStream("/images/contineue.png"));
            logo = new Image(getClass().getResourceAsStream("/images/ModEx_rmbg.png"));

            deliveryBtnImage = new Image(getClass().getResourceAsStream("/images/delivery_btn.png"));

            TruckImages[0] = new Image(getClass().getResourceAsStream("/images/truck_blue.png"));
            TruckImages[1] = new Image(getClass().getResourceAsStream("/images/truck_green.png"));
            TruckImages[2] = new Image(getClass().getResourceAsStream("/images/truck_lightblue.png"));
            TruckImages[3] = new Image(getClass().getResourceAsStream("/images/truck_orange.png"));
            TruckImages[4] = new Image(getClass().getResourceAsStream("/images/truck_purple.png"));
            TruckImages[5] = new Image(getClass().getResourceAsStream("/images/truck_red.png"));
            TruckImages[6] = new Image(getClass().getResourceAsStream("/images/truck_yellow.png"));

        } catch (Exception e) {
            System.out.println("⚠️ โหลดรูปไม่สำเร็จ! เช็กว่าวางไฟล์รูปถูกที่หรือยัง");
        }

        root = new StackPane();
        mainGameContent = new StackPane();

        Pane gameMap = new Pane();
        gameMap.setStyle("-fx-background-color: #4a627b;");
        gameMap.getTransforms().addAll(mapTranslate, mapScale);

        drawThailand(gameMap);

        gameMap.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {

                if (!isZoomed) {
                    int gridCol = (int) (e.getX() / GRID_SIZE);
                    int gridRow = (int) (e.getY() / GRID_SIZE);

                    double targetCenterX = (gridCol * GRID_SIZE) + (GRID_SIZE / 2.0);
                    double targetCenterY = (gridRow * GRID_SIZE) + (GRID_SIZE / 2.0);

                    zoomToArea(root, targetCenterX, targetCenterY, 10.0);

                    isZoomed = true;

                } else {
                    resetZoom(root);

                    isZoomed = false;
                }

                e.consume();
            }
        });

        uiLayer = new AnchorPane();
        uiLayer.setPickOnBounds(false);

        moneyLabel = new Label("฿ 5,000");
        moneyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 10px;");

        AnchorPane conveyor = new AnchorPane();
        AnchorPane.setBottomAnchor(conveyor, 0.0);
        AnchorPane.setLeftAnchor(conveyor, 0.0);
        AnchorPane.setRightAnchor(conveyor, 0.0);
        conveyor.getStyleClass().add("Conveyor");

        conveyor_tile = new AnchorPane();
        AnchorPane.setBottomAnchor(conveyor_tile, 0.0);
        AnchorPane.setLeftAnchor(conveyor_tile, 0.0);
        AnchorPane.setRightAnchor(conveyor_tile, 0.0);
        conveyor_tile.getStyleClass().add("Conveyor_tile");

        conveyor_slot = new HBox();
        conveyor_slot.getStyleClass().add("Conveyor_slot");
        conveyor_slot.setSpacing(10.0);
        conveyor_tile.getChildren().add(conveyor_slot);
        AnchorPane.setTopAnchor(conveyor_slot, 0.0);
        AnchorPane.setBottomAnchor(conveyor_slot, 0.0);
        AnchorPane.setLeftAnchor(conveyor_slot, 0.0);
        AnchorPane.setRightAnchor(conveyor_slot, 0.0);

        AnchorPane conveyor_label = new AnchorPane();
        AnchorPane.setTopAnchor(conveyor_label, 0.0);
        AnchorPane.setLeftAnchor(conveyor_label, 0.0);
        AnchorPane.setRightAnchor(conveyor_label, 0.0);
        conveyor_label.getStyleClass().add("Conveyor_label");

        inbound_label = new Label("INBOUND QUEUE");
        inbound_label.getStyleClass().add("Conveyor_Inbound_label");
        AnchorPane.setTopAnchor(inbound_label, 0.0);
        AnchorPane.setBottomAnchor(inbound_label, 0.0);
        AnchorPane.setLeftAnchor(inbound_label, 0.0);

        conveyor_label.getChildren().add(inbound_label);

        conveyor.getChildren().addAll(conveyor_label, conveyor_tile);

        uiLayer.getChildren().addAll(moneyLabel, conveyor);

        renderClock(uiLayer);


        mainGameContent.getChildren().addAll(gameMap, uiLayer);
        mainGameContent.setStyle("-fx-background-color: #4a627b;");
        root.getChildren().add(mainGameContent);
        root.setStyle("-fx-background-color: #4a627b;");



        root.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                resetZoom(root);

                isZoomed = false;
            }
        });

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/ModEx.png")));
        stage.setFullScreen(true);
        stage.setTitle("MODEx");
        stage.setScene(scene);
        stage.show();

        gameController = new GameController(this);

        //gameController.start();

        showStartScreen();

        drawTruckMenu();
    }

    public void drawThailand(Pane uiLayer) {
        double[][] coordinates = {{102.91358482584052,11.64590072271301},{102.91380048962824,11.765536132387345},{102.69996341303356,12.13874332298333},{102.76512739604206,12.416349137015542},{102.55862837360664,12.631219757794074},{102.47315556405641,13.006132356024628},{102.3282031808431,13.275159935427537},{102.33223393524663,13.564599567097213},{102.51320478338283,13.56715740458737},{102.6986198285852,13.761796738783948},{102.86977218963861,14.020618283245728},{102.91860639172873,14.18549174044597},{103.0850045274609,14.29582110411102},{103.55024744036682,14.421859869055998},{103.87033123882914,14.343259846738956},{104.24746585687359,14.401085886437304},{104.461974731115,14.35710926643189},{104.77167158120268,14.439869015398449},{104.97382977977996,14.380647783212808},{105.0473136958266,14.214069019863864},{105.15500736002329,14.330470036220653},{105.41597334512545,14.428164346997304},{105.51012779687746,14.593580547023299},{105.48914717002357,14.78633367776142},{105.58356000121869,14.977588079523741},{105.44377526941568,15.133806052848618},{105.56397465089594,15.272531439062393},{105.65099776360199,15.63460236652548},{105.44796106281574,15.764878660357603},{105.37256514617728,15.88213247461663},{105.40491459545741,16.018816652152505},{105.0424560929676,16.14144478466121},{105.01548099577043,16.276733689900095},{104.7545150098149,16.52891466062536},{104.77833784543922,16.715828320829186},{104.74531661830925,17.024801788515724},{104.8099121401446,17.171666092513007},{104.81642337163997,17.372790777054483},{104.714517461772,17.515262784375125},{104.4811983468143,17.64042296717412},{104.11093673418647,18.114554384028796},{104.00003910610604,18.318443723068008},{103.83209068037893,18.320665683509407},{103.39676841520301,18.441407673239055},{103.26075603933836,18.400195682702623},{103.03792728020873,17.99068589370008},{102.93860516068942,18.00892757562692},{102.59557704189388,17.850074457199135},{102.56751674831773,17.97084232806499},{102.30670575761356,18.05145734412633},{102.07850263046942,18.21379884360412},{101.90430141120287,18.037117323034725},{101.79040651955611,18.07357483849325},{101.72906661595297,17.91218946887822},{101.57755131372697,17.868109458531993},{101.24702885646211,17.592157327615304},{101.13230716631861,17.461674273281957},{100.9019336561809,17.561926705453132},{101.00828374105129,17.895239617975143},{101.1651734005237,18.053421240746157},{101.14553632139614,18.336246143716817},{101.03029790541095,18.42779111605104},{101.24010423216886,18.673641811943885},{101.23292116592522,18.892723790576493},{101.31787725480808,19.050130409557013},{101.22914879417374,19.14358708043388},{101.19287196426193,19.45279316581963},{101.1246590777529,19.56511191690264},{100.87459678499435,19.61291239674149},{100.74550908593598,19.485013390018924},{100.46196047227936,19.53710306604301},{100.38351567917768,19.763859432160523},{100.54335087111126,20.066579895648214},{100.36439538318346,20.368964522603623},{100.09929529080628,20.31780488639062},{99.94204390416829,20.444024552143016},{99.76959965291103,20.32842438819667},{99.50289758447971,20.345219226691242},{99.51033898874037,20.15380971147109},{99.29567509005915,20.062497457348826},{99.11749475044245,20.117791236703432},{99.01682906088352,20.04100006030952},{99.00876754746417,19.845921592073132},{98.80784958817074,19.80654420199438},{98.53262091110275,19.675854516006044},{98.21791142265756,19.707971320889662},{98.01792364182924,19.789387619889492},{98.00831179671422,19.639112373216552},{97.83984661847047,19.55531898936731},{97.76708624312732,19.39752525360911},{97.81080450438598,19.11221966060738},{97.65784224154245,18.92571927791198},{97.71913051069414,18.86463785789076},{97.75179001742289,18.582484550176716},{97.43811403927292,18.48812317382664},{97.5605872089073,18.328262288418312},{97.75406375294989,17.96903345134999},{97.68554080350172,17.88071842631498},{97.7689465643101,17.67918033769712},{97.98278364186052,17.50544417589623},{98.10060591203573,17.302614206197433},{98.3133061143428,17.05203523862282},{98.50946984387733,16.892665066661966},{98.45665654306885,16.723218132016203},{98.63163292294462,16.463130521404555},{98.90334762436727,16.36344681288193},{98.80722945240194,16.110542300730295},{98.65871138810846,16.116278402140203},{98.58378055669618,15.976958704172217},{98.54305952657676,15.735371375065567},{98.55990604507369,15.355317064132837},{98.28302372118429,15.289972223078948},{98.1649947514443,15.125796207273357},{98.24333624104746,14.80511824730103},{98.4182092763994,14.607559159966314},{98.54771041656055,14.37767655236228},{98.94530888942855,14.06914243890252},{99.1525313625437,13.714874494526837},{99.19015182099909,13.229503734106457},{99.1020951867732,13.171341960922977},{99.21412967252053,12.734650098869462},{99.39375695727564,12.589775282319284},{99.38703902720212,12.465906928565657},{99.46631066317316,12.126030920132509},{99.63002160248772,11.81576565109561},{99.24327519072078,11.197663908915635},{99.21299279939146,11.108263666930089},{99.00153283378991,10.957626815933233},{98.76671513078962,10.688754406420685},{98.7913131116349,10.520418403276821},{98.72046959671616,10.227932029639478},{98.51270592488206,9.83685944054019},{98.47169030135716,9.621161217205088},{98.32691490901881,9.203680717957928},{98.38200930072074,9.071926035449486},{98.22413170545539,8.735337610356611},{98.19947349939946,8.534369142068305},{98.27759851330121,8.240708863601748},{98.42497805949222,8.14842354749716},{98.4456486249004,8.325099868862198},{98.59888755789672,8.374904572276659},{98.74415124067367,8.21491123948011},{98.74740644454697,8.068264043190991},{98.91797937167925,8.05133711128284},{99.13754315693379,7.744330019992482},{99.29273522338954,7.620672940797892},{99.3658960290451,7.336371145550467},{99.52466881995379,7.311346811041757},{99.58399498613723,7.15643948932259},{99.69288170703717,7.116115627613852},{99.68580163259601,6.877346157014271},{99.79721113548005,6.8216820582386966},{100.0013126934094,6.567450216660077},{100.14435713725727,6.479579777538759},{100.16683638926871,6.695122228315446},{100.28078291735879,6.688920971736038},{100.38739140188413,6.522083689532303},{100.80560876896405,6.4148034349849405},{100.82214522218395,6.259877389393403},{101.08166426644141,6.24646739491401},{101.0711222720557,5.919846460867732},{100.96720096603077,5.781017724814823},{101.1054354263371,5.637641506453733},{101.24899255332674,5.786986376328636},{101.53465987252372,5.906023079228305},{101.65036340494181,5.782645586659046},{101.80048343818086,5.739909143590271},{102.06000248275728,6.094797071673623},{102.07309003822779,6.257513707562739},{101.77165775122212,6.500718547817764},{101.56625409926804,6.832220724275055},{101.38520969636266,6.905220341031205},{100.99757308536844,6.85667530232629},{100.76514733062207,6.983140344139118},{100.61109459293218,7.19261295695754},{100.39551842286482,7.211330436332067},{100.18539472638162,7.514227596270112},{100.1429142577906,7.7000186033528255},{100.3069767530448,7.757879858921118},{100.32349694849498,7.616400574423759},{100.44019616117839,7.476996179568428},{100.22046960515905,8.448716654663196},{100.10434003948451,8.417669981657369},{99.95923913212503,8.637640735494813},{99.91627036765128,9.101996010881656},{99.85352624788992,9.294663932153872},{99.68946372774784,9.315863215242699},{99.47478274974009,9.200995206868546},{99.2550561765257,9.232489200642537},{99.32357831871724,9.391546912576066},{99.18140710807342,9.643256066039815},{99.15992272180848,10.12832265571814},{99.23414147201913,10.34369538022185},{99.244639517909,10.535142307568895},{99.51156660236914,10.902411204034173},{99.49415123714267,11.114569393832701},{99.58619225502952,11.195135819906238},{99.56934654744047,11.333970418265885},{99.75180097329535,11.709865589832667},{99.81706790735171,11.741441171502117},{99.85531660666936,11.96841059458946},{100.02003014266663,12.192694390291013},{99.96094811400955,12.625392980224944},{99.98764081960957,12.790472710500104},{100.10434004834022,13.057318499976091},{99.96713299941057,13.259019190587262},{100.03882895864177,13.402736607840586},{100.23715254512852,13.477362415051855},{100.64966879885058,13.52024959920699},{100.95069420289514,13.468247754754854},{100.98129316820015,13.359035575967242},{100.87826581414252,13.098333992691508},{100.8374129469793,12.707912411956968},{100.92611738215712,12.61912667831006},{101.08773846372706,12.680609322710518},{101.4428817138891,12.624823371035573},{101.83025150718666,12.672797024023968},{102.0719507199565,12.48851148681527},{102.29590905219524,12.190008876463416},{102.74968509844989,12.040269333760344},{102.77702883489393,11.902533202142202},{102.91358482584052,11.64590072271301}};

        double minX = 97.34, maxX = 105.65;
        double minY = 5.61, maxY = 20.46;

        double midX = (minX + maxX) / 2;
        double midY = (minY + maxY) / 2;

        double scale = 50.0;

        Polygon thailand = new Polygon();
        for (double[] point : coordinates) {
            double x = (point[0] - midX) * scale;
            double y = (midY - point[1]) * scale;
            thailand.getPoints().addAll(x, y);
        }

        thailand.setFill(Color.web("#1d2632"));
        thailand.setStroke(Color.web("#1d2632"));
        thailand.setStrokeWidth(8);
        thailand.setStrokeLineJoin(StrokeLineJoin.ROUND);

        double[][] coordinates_phuket = {{98.41863041172863, 7.903754076193204}, {98.43848718055366, 8.065741361636915}, {98.2814233667237, 8.191799127644858}, {98.26775148931516, 7.89008196439631}, {98.41863041172863, 7.903754076193204}};

        Polygon phuket = new Polygon();
        for (double[] point : coordinates_phuket) {
            double x = (point[0] - midX) * scale;
            double y = (midY - point[1]) * scale;
            phuket.getPoints().addAll(x, y);
        }

        phuket.setFill(Color.web("#1d2632"));
        phuket.setStroke(Color.web("#1d2632"));
        phuket.setStrokeWidth(1);
        phuket.setStrokeLineJoin(StrokeLineJoin.ROUND);

        this.mapGroup = new Group(thailand, phuket);

        this.mapGroup.getStyleClass().add("map-polygon");

        DropShadow shadow = new DropShadow();

        shadow.setRadius(10);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.color(0, 0, 0, 0.5));

        this.mapGroup.setEffect(shadow);

        this.mapGroup.layoutXProperty().bind(uiLayer.widthProperty().divide(2));
        this.mapGroup.layoutYProperty().bind(uiLayer.heightProperty().subtract(121).divide(2));

        DoubleBinding dynamicScale = Bindings.createDoubleBinding(
                () -> (uiLayer.getHeight() - 121) / 800.0,
                uiLayer.heightProperty()
        );

        this.mapGroup.scaleXProperty().bind(dynamicScale);
        this.mapGroup.scaleYProperty().bind(dynamicScale);

        uiLayer.getChildren().add(this.mapGroup);
    }

    public boolean isDrawProvinces() {
        return isDrawProvinces;
    }
    public void setDrawProvinces(boolean drawProvinces) {
        isDrawProvinces = drawProvinces;
    }

    private void zoomToArea(StackPane root, double targetX, double targetY, double targetScale) {
        double screenCenterX = root.getWidth() / 2;
        double screenCenterY = root.getHeight() / 2;

        double newTx = screenCenterX - (targetX * targetScale);
        double newTy = screenCenterY - (targetY * targetScale);

        System.out.println("targetX: " + targetX + " targetY: " + targetY);
        System.out.println("newTx: " + newTx + " newTy: " + newTy);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(mapTranslate.xProperty(), newTx, Interpolator.EASE_BOTH),
                        new KeyValue(mapTranslate.yProperty(), newTy, Interpolator.EASE_BOTH),
                        new KeyValue(mapScale.xProperty(), targetScale, Interpolator.EASE_BOTH),
                        new KeyValue(mapScale.yProperty(), targetScale, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    private void renderClock(AnchorPane root) {
        clockPane = new AnchorPane();

        clockPane.setPrefSize(60, 60);
        clockPane.setMinSize(60, 60);
        clockPane.setMaxSize(60, 60);

        AnchorPane.setRightAnchor(clockPane, 10.0);
        AnchorPane.setTopAnchor(clockPane, 10.0);
        clockPane.getStyleClass().add("clock-pane");
        clockPane.setStyle("-fx-background-color: #292d32;");

        double centerX = 30.0;
        double centerY = 30.0;

        this.hourHand = new Line(centerX, centerY, centerX, 18);
        this.hourHand.setStrokeWidth(3.5);
        this.hourHand.setStroke(Color.web("white"));
        this.hourHand.setStrokeLineCap(StrokeLineCap.ROUND);

        this.minuteHand = new Line(centerX, centerY, centerX, 12);
        this.minuteHand.setStrokeWidth(3.5);
        this.minuteHand.setStroke(Color.web("white"));
        this.minuteHand.setStrokeLineCap(StrokeLineCap.ROUND);

        hourRotate = new Rotate(0, centerX, centerY);
        minuteRotate = new Rotate(0, centerX, centerY);

        this.hourHand.getTransforms().add(hourRotate);
        this.minuteHand.getTransforms().add(minuteRotate);

        timeMenu = new VBox(10);
        timeMenu.getStyleClass().add("time-menu");

        Button btnResume = new Button();
        Button btnFastForward = new Button();

        btnResume.getStyleClass().addAll("menu-btn", "timespeed-I");
        btnFastForward.getStyleClass().addAll("menu-btn", "timespeed-II");

        timeMenu.getChildren().addAll(btnResume, btnFastForward);
        
        AnchorPane.setRightAnchor(timeMenu, 20.0);
        AnchorPane.setTopAnchor(timeMenu, 80.0);

        btnResume.setOnAction(e -> {
            gameController.getTimeManager().changeTickSpeed(1);
        });

        btnFastForward.setOnAction(e -> {
            gameController.getTimeManager().changeTickSpeed(2);
        });

        timeMenu.setVisible(false);
        timeMenu.setOpacity(0);

        for (Node btn : timeMenu.getChildren()) {
            btn.setOpacity(0);
        }
        
        clockPane.setCursor(Cursor.HAND);
        clockPane.setOnMouseClicked(e -> toggletimeMenu());

        clockPane.getChildren().addAll(this.hourHand, this.minuteHand);
        root.getChildren().addAll(timeMenu, clockPane);
    }

    private void toggletimeMenu() {
        Timeline timeline = new Timeline();


        if (!isMenuOpen) {
            timeMenu.setVisible(true);

            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(timeMenu.opacityProperty(), 1)
                    )
            );

            for (int i = 0; i < timeMenu.getChildren().size(); i++) {
                Node btn = timeMenu.getChildren().get(i);

                int delay = 150 + (i * 500);

                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(delay),
                                new KeyValue(btn.opacityProperty(), 1)
                        )
                );
            }

            timeline.setOnFinished(null);

        } else {

            for (Node btn : timeMenu.getChildren()) {
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(btn.opacityProperty(), 0)
                        )
                );
            }
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(timeMenu.opacityProperty(), 0)
                    )
            );

            timeline.setOnFinished(e -> timeMenu.setVisible(false));
        }

        timeline.play();
        isMenuOpen = !isMenuOpen;
    }

    public void updateClock(double hour, double minute, boolean isNightTime) {
        double minuteAngle = minute * 6.0;

        double hourAngle = (hour % 12) * 30.0;

        minuteRotate.setAngle(minuteAngle);
        hourRotate.setAngle(hourAngle);

        if (isNightTime) {
            clockPane.setStyle("-fx-background-color: #292d32;");
            minuteHand.setStroke(Color.web("white"));
            hourHand.setStroke(Color.web("white"));
        } else {
            clockPane.setStyle("-fx-background-color: white;");
            minuteHand.setStroke(Color.web("black"));
            hourHand.setStroke(Color.web("black"));
        }
    }

    public void updateMoneyLabel(int money) {
        moneyLabel.setText("฿ " + String.format("%,d", money));
    }

    private void resetZoom(StackPane root) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(mapTranslate.xProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(mapTranslate.yProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(mapScale.xProperty(), 1.0, Interpolator.EASE_BOTH),
                        new KeyValue(mapScale.yProperty(), 1.0, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    public void drawProvinceNode(Province node) {
        if (nodeSprites.containsKey(node)) return;

        double x = (node.lon - midX) * MAP_SCALE;
        double y = (midY - node.lat) * MAP_SCALE;

        ImageView sprite = new ImageView(node.isUnlocked ? (node.isStartNode ? startNodeImage : unlockedImage) : lockedImage);

        sprite.setFitWidth(SPRITE_SIZE);
        sprite.setFitHeight(SPRITE_SIZE);

        sprite.setX(x - (SPRITE_SIZE / 2));
        sprite.setY(y - (SPRITE_SIZE / 2));

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(25), sprite);
        scaleIn.setToX(1.2);
        scaleIn.setToY(1.2);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(25), sprite);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        scaleOut.setInterpolator(Interpolator.EASE_OUT);

        sprite.setOnMouseEntered(e -> {
            if (!isZoomed || node.isUnlocked) return;
            sprite.setCursor(Cursor.HAND);
            scaleOut.stop();
            scaleIn.playFromStart();
        });

        sprite.setOnMouseExited(e -> {
            sprite.setCursor(Cursor.DEFAULT);
            scaleIn.stop();
            scaleOut.playFromStart();
        });

        sprite.setOnMouseClicked(e -> {
            if (!isZoomed) return;
            if (e.getButton() == MouseButton.PRIMARY) {
                if (node.isConstructing) {
                    showConstructionModal(node);
                } else if (!node.isUnlocked) {
                    showPurchaseModal(node);
                } else {
                    System.out.println("จังหวัดนี้เป็นของคุณแล้ว!");
                }
            }
        });

        nodeSprites.put(node, sprite);
        mapGroup.getChildren().add(sprite);
    }

    public void updateNodeToConstructing(Province node) {
        ImageView sprite = nodeSprites.get(node);
        if (sprite != null) {
            sprite.setImage(constructionImage);

            FadeTransition ft = new FadeTransition(Duration.millis(800), sprite);
            ft.setFromValue(0.4);
            ft.setToValue(1.0);
            ft.setCycleCount(Animation.INDEFINITE);
            ft.setAutoReverse(true);
            ft.play();

            sprite.setUserData(ft);
        }
    }

    public void updateNodeColor(Province node) {
        ImageView sprite = nodeSprites.get(node);
        if (sprite != null) {
            if (sprite.getUserData() instanceof FadeTransition) {
                ((FadeTransition) sprite.getUserData()).stop();
                sprite.setUserData(null);
            }

            if (node.isStartNode) {
                sprite.setImage(startNodeImage);
            } else {
                sprite.setImage(unlockedImage);
            }

            sprite.setOpacity(1.0);
            sprite.setStyle("");
        }
    }

    public void updateEdgeColor(Province source, Province target, String hexColor, double width, boolean bringToFront) {
        String edgeKey = getEdgeKey(source, target);
        Line line = edgeLines.get(edgeKey);
        if (line != null) {
            line.setStroke(Color.web(hexColor));
            line.setStrokeWidth(width);
            if (bringToFront) {
                line.toFront(); // ดันเส้นไฮไลต์ขึ้นมาไม่ให้โดนเส้นอื่นทับ
            }
        }
    }

    public void updateEdgeColor(Province source, Province target, String hexColor) {
        updateEdgeColor(source, target, hexColor, 0.8, false);
    }

    public void updateEdgeColor(Province source, Province target) {
        updateEdgeColor(source, target, "#4ade80", 0.8, false);
    }

    private String getEdgeKey(Province a, Province b) {
        int min = Math.min(a.id, b.id);
        int max = Math.max(a.id, b.id);
        return min + "-" + max;
    }

    public void drawEdge(Province source, Province target, boolean isUnlocked) {
        String edgeKey = getEdgeKey(source, target);
        if (edgeLines.containsKey(edgeKey)) return;

        double x1 = (source.lon - midX) * MAP_SCALE;
        double y1 = (midY - source.lat) * MAP_SCALE;
        double x2 = (target.lon - midX) * MAP_SCALE;
        double y2 = (midY - target.lat) * MAP_SCALE;

        Line line = new Line(x1, y1, x2, y2);

        line.setStroke(isUnlocked ? Color.web("#4ade80") : Color.web("#5a6b7d"));
        line.setStrokeWidth(isUnlocked ? 0.8 : 0.4);
        line.setOpacity(0.6);

        edgeLines.put(edgeKey, line);
        mapGroup.getChildren().add(1, line);
    }


    private void showPurchaseModal(Province node) {
        modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxSize(800, 400);
        dialogBox.setSpacing(10);

        Label title = new Label("UNLOCK PROVINCE");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label provincename = new Label(node.name);
        provincename.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold; -fx-text-alignment: center;");

        Label desc = new Label("฿ " + String.format("%,d", gameController.getCurrentUnlockCost()));
        desc.setStyle("-fx-text-fill: lightgray; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label constuctiontime = new Label("24 hours (In-game)");
        if (gameController.getUnlockNodeCounts() <= 5) {
            constuctiontime = new Label("1 hours (In-game)");
        }


        constuctiontime.setStyle("-fx-text-fill: lightgray; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(20);
        buttonBox.setTranslateY(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnBuy = new Button("CONFIRM");
        btnBuy.getStyleClass().add("primary-btn");

        if (gameController.getMoney() < gameController.getCurrentUnlockCost()) {
            desc.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
            btnBuy.setDisable(true);
        }

        Button btnCancel = new Button("CANCEL");
        btnCancel.getStyleClass().add("secondary-btn");

        btnBuy.setOnAction(e -> {
            closeModal();
            gameController.tryUnlockProvince(node);
        });

        btnCancel.setOnAction(e -> closeModal());

        buttonBox.getChildren().addAll(btnBuy, btnCancel);
        dialogBox.getChildren().addAll(title, provincename, desc, constuctiontime, buttonBox);
        modalOverlay.getChildren().add(dialogBox);

        GaussianBlur blur = new GaussianBlur(15);
        mainGameContent.setEffect(blur);

        root.getChildren().add(modalOverlay);

        ScaleTransition st = new ScaleTransition(Duration.millis(250), dialogBox);
        st.setFromX(0.5); st.setFromY(0.5);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    private void showConstructionModal(Province node) {
        modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxSize(800, 400);
        dialogBox.setSpacing(10);

        Label title = new Label("UNDER CONSTRUCTION");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label provincename = new Label(node.name);
        provincename.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: bold; -fx-text-alignment: center;");

        Label percentLabel = new Label("- 0.00 % -");
        percentLabel.setStyle("-fx-text-fill: lightgray; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px;  -fx-text-alignment: center;");

        HBox buttonBox = new HBox(20);
        buttonBox.setTranslateY(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnClose = new Button("CLOSE");
        btnClose.getStyleClass().add("secondary-btn");

        AnimationTimer liveUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double currentExactHour = gameController.getTimeManager().getTotalHours() + (gameController.getTimeManager().getMinute() / 60.0);

                double remainingHours = node.constructionFinishHour - currentExactHour;

                remainingHours = Math.max(0, Math.min(24, remainingHours));

                double percentCompleted = ((24.0 - remainingHours) / 24.0) * 100.0;

                if (percentCompleted >= 100.0) {
                    percentLabel.setText("COMPLETED!");
                    percentLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 22px; -fx-font-weight: bold;");
                    this.stop();
                    closeModal();
                } else {
                    percentLabel.setText("- " + String.format("%.2f %%", percentCompleted) + " -");
                }
            }
        };
        liveUpdateTimer.start();

        btnClose.setOnAction(e -> {
            liveUpdateTimer.stop();
            closeModal();
        });

        buttonBox.getChildren().add(btnClose);
        dialogBox.getChildren().addAll(title, provincename, percentLabel, buttonBox);
        modalOverlay.getChildren().add(dialogBox);

        GaussianBlur blur = new GaussianBlur(15);
        mainGameContent.setEffect(blur);

        root.getChildren().add(modalOverlay);

        ScaleTransition st = new ScaleTransition(Duration.millis(250), dialogBox);
        st.setFromX(0.5); st.setFromY(0.5);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    private void closeModal() {
        mainGameContent.setEffect(null);
        root.getChildren().remove(modalOverlay);
    }

    private Timeline spinConveyor(Parcel parcel) {
        javafx.beans.property.IntegerProperty bgPositionX = new javafx.beans.property.SimpleIntegerProperty(0);

        bgPositionX.addListener((obs, oldVal, newVal) -> {
            conveyor_tile.setStyle("-fx-background-position: " + newVal + "px 0px;");
        });

        Timeline beltAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(bgPositionX, 0)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(bgPositionX, 40, Interpolator.LINEAR))
        );

        beltAnimation.setCycleCount(Animation.INDEFINITE);
        beltAnimation.play();
        return beltAnimation;
    }

    public void drawParcelOnConveyor(Parcel parcel) {
        if (conveyorQueue.size() >= 20) return;
        StackPane parcelNode = new StackPane();
        parcelNode.setUserData(parcel);

        parcelNode.getStyleClass().add("parcel-box");
        parcelNode.setPrefSize(110, 60);
        parcelNode.setStyle("-fx-background-color: #dcb382; -fx-border-color: #8c5d2c; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        parcelNode.setLayoutY(15.5);

        Label destinationLabel = new Label(parcel.getTo().name);
        parcelNode.getChildren().add(destinationLabel);

        parcelNode.setTranslateX(-120.0);

        conveyor_tile.getChildren().add(parcelNode);
        conveyorQueue.add(parcelNode);

        Timeline spinAnim = spinConveyor(parcel);
        parcelNode.getProperties().put("spinAnim", spinAnim);

        setupDragAndDrop(parcelNode);

        updateQueuePositions();

        inbound_label.setText("INBOUND QUEUE ("+conveyorQueue.size() + "/20)");
    }

    private void updateQueuePositions() {
        double currentWidth = conveyor_tile.getWidth() > 0 ? conveyor_tile.getWidth() : 900.0;
        double speed = 250.0;

        for (int i = 0; i < conveyorQueue.size(); i++) {
            Node node = conveyorQueue.get(i);

            if (node.getUserData() instanceof Timeline) {
                ((Timeline) node.getUserData()).stop();
            }

            double startX = node.getTranslateX();

            double targetX = currentWidth - 130 - (i * 120);

            double distance = Math.abs(targetX - startX);
            double durationSeconds = Math.max(0.05, distance / speed);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), startX)),
                    new KeyFrame(Duration.seconds(durationSeconds), new KeyValue(node.translateXProperty(), targetX, Interpolator.LINEAR))
            );


            timeline.setOnFinished(e -> {
                if (node.getProperties().containsKey("spinAnim")) {
                    Timeline spinAnim = (Timeline) node.getProperties().get("spinAnim");
                    if (spinAnim != null) spinAnim.stop();
                }
            });

            node.getProperties().put("moveAnim", timeline);
            timeline.play();


            if (distance > 1 && node.getProperties().containsKey("spinAnim")) {
                Timeline spinAnim = (Timeline) node.getProperties().get("spinAnim");
                if(spinAnim != null) spinAnim.play();
            }
        }
    }

    private void setupDragAndDrop(StackPane parcelNode) {
        final double[] dragDelta = new double[2];

        parcelNode.setOnMousePressed(e -> {

            if (parcelNode.getProperties().containsKey("moveAnim")) {
                Timeline moveAnim = (Timeline) parcelNode.getProperties().get("moveAnim");
                if (moveAnim != null) moveAnim.stop();
            }
            if (parcelNode.getProperties().containsKey("spinAnim")) {
                Timeline spinAnim = (Timeline) parcelNode.getProperties().get("spinAnim");
                if (spinAnim != null) spinAnim.stop();
            }

            parcelNode.setCursor(Cursor.CLOSED_HAND);


            javafx.geometry.Point2D scenePos = parcelNode.localToScene(0, 0);
            javafx.geometry.Point2D uiLayerPos = uiLayer.sceneToLocal(scenePos);


            if (parcelNode.getParent() != uiLayer) {
                ((Pane) parcelNode.getParent()).getChildren().remove(parcelNode);
                uiLayer.getChildren().add(parcelNode);
            }


            parcelNode.toFront();


            parcelNode.setLayoutX(0);
            parcelNode.setLayoutY(0);
            parcelNode.setTranslateX(uiLayerPos.getX());
            parcelNode.setTranslateY(uiLayerPos.getY());


            dragDelta[0] = parcelNode.getTranslateX() - e.getSceneX();
            dragDelta[1] = parcelNode.getTranslateY() - e.getSceneY();


            if (conveyorQueue.remove(parcelNode)) {
                updateQueuePositions();
            }
        });

        parcelNode.setOnMouseDragged(e -> {
            parcelNode.setTranslateX(e.getSceneX() + dragDelta[0]);
            parcelNode.setTranslateY(e.getSceneY() + dragDelta[1]);
        });

        parcelNode.setOnMouseReleased(e -> {
            parcelNode.setCursor(Cursor.HAND);
            javafx.geometry.Bounds parcelBounds = parcelNode.localToScene(parcelNode.getBoundsInLocal());

            boolean droppedInTruck = false;

            for (int i = 0; i < truckSlots.length; i++) {
                StackPane targetSlot = truckSlots[i];
                javafx.geometry.Bounds slotBounds = targetSlot.localToScene(targetSlot.getBoundsInLocal());

                if (slotBounds.intersects(parcelBounds) && targetSlot.getChildren().isEmpty()) {
                    droppedInTruck = true;

                    int logicalOrder = 4 - i;
                    System.out.println("🚚 พัสดุลงรถ Order ที่: " + logicalOrder);

                    uiLayer.getChildren().remove(parcelNode);
                    targetSlot.getChildren().add(parcelNode);

                    parcelNode.setTranslateX(0);
                    parcelNode.setTranslateY(0);

                    // 🌟 เพิ่มบรรทัดนี้: อัปเดตสีเส้น เมื่อวางของลงรถสำเร็จ
                    highlightTruckRoute();
                    break;
                }
            }

            if (droppedInTruck) return;

            uiLayer.getChildren().remove(parcelNode);
            conveyor_tile.getChildren().add(parcelNode);

            parcelNode.setLayoutY(15.5);

            javafx.geometry.Point2D localPos = conveyor_tile.sceneToLocal(parcelBounds.getMinX(), parcelBounds.getMinY());
            parcelNode.setTranslateX(localPos.getX());
            parcelNode.setTranslateY(0);

            int insertIndex = conveyorQueue.size();
            for (int i = 0; i < conveyorQueue.size(); i++) {
                if (parcelNode.getTranslateX() > conveyorQueue.get(i).getTranslateX()) {
                    insertIndex = i;
                    break;
                }
            }

            conveyorQueue.add(insertIndex, parcelNode);
            updateQueuePositions();

            // 🌟 เพิ่มบรรทัดนี้: อัปเดตสีเส้นอีกครั้ง กรณีผู้เล่นดึงของออกจากรถมาไว้ที่สายพาน
            highlightTruckRoute();
        });

        parcelNode.setOnMouseEntered(e -> parcelNode.setCursor(Cursor.HAND));
    }

    // 🌟 1. ฟังก์ชันสำหรับล้างสีไฮไลต์ ให้กลับเป็นสีเขียว/เทา ปกติ
    public void resetRouteHighlight() {
        if (gameController == null || gameController.getProvinceGraph() == null) return;

        for (Province node : gameController.getProvinceGraph().getAllNodes()) {
            for (Province neighbor : gameController.getProvinceGraph().getNeighbors(node)) {
                if (node.isUnlocked && neighbor.isUnlocked) {
                    updateEdgeColor(node, neighbor, "#4ade80", 0.8, false);
                } else {
                    updateEdgeColor(node, neighbor, "#5a6b7d", 0.4, false);
                }
            }
        }
    }

    // 🌟 2. ฟังก์ชันตีเส้นไฮไลต์สีต่างๆ ตามลำดับกล่องในรถบรรทุก
    public void highlightTruckRoute() {
        resetRouteHighlight(); // ล้างเส้นเก่าก่อนเสมอ

        java.util.List<Parcel> parcels = getParcelsInTruck();
        if (parcels.isEmpty()) return; // ถ้ารถว่าง ไม่ต้องทำอะไร

        Province currentLoc = parcels.get(0).getFrom(); // รถเริ่มจากจุดรับของกล่องแรก

        // ชุดสีสำหรับเรียงลำดับ (เหลือง -> ฟ้า -> ม่วง -> ชมพู -> ส้ม)
        String[] routeColors = {"#facc15", "#38bdf8", "#c084fc", "#f472b6", "#fb923c"};

        for (int i = 0; i < parcels.size(); i++) {
            Parcel p = parcels.get(i);

            // สำคัญมาก: ล้างสถานะ Graph ก่อนคำนวณเส้นทางใหม่
            for (Province node : gameController.getProvinceGraph().getAllNodes()) {
                node.isVisited = false;
                node.distanceFormSource = Double.MAX_VALUE;
                node.from = null;
            }

            // หาเส้นทางย่อยของคิวนี้
            List<Edge> path = gameController.getProvinceGraph().findShortestPath(currentLoc, p.getTo());

            if (path != null) {
                String color = routeColors[i % routeColors.length]; // ดึงสีตามลำดับคิว
                for (Edge edge : path) {
                    updateEdgeColor(edge.source, edge.target, color, 3.0, true);
                }
            }
            // อัปเดตจุดเริ่มของรอบถัดไปเป็นปลายทางของกล่องนี้
            currentLoc = p.getTo();
        }
    }

    public void drawTruckMenu() {
        truckSlots = new StackPane[5];
        truckMenu = new AnchorPane();
        truckMenu.getStyleClass().add("truck-box");
        truckMenu.setPrefSize(130*5 + 10, 120);
        uiLayer.getChildren().add(truckMenu);
        AnchorPane.setRightAnchor(truckMenu, 5.0);
        AnchorPane.setBottomAnchor(truckMenu, 126.0);

        AnchorPane LabelArea = new AnchorPane();
        LabelArea.getStyleClass().add("truck-label");
        LabelArea.setPrefSize(130*5 + 10, 40);
        AnchorPane.setLeftAnchor(LabelArea, 0.0);
        AnchorPane.setRightAnchor(LabelArea, 0.0);
        AnchorPane.setTopAnchor(LabelArea, 0.0);

        Label trucktext = new Label("Truck");
        trucktext.getStyleClass().add("truck-text");
        LabelArea.getChildren().add(trucktext);
        AnchorPane.setLeftAnchor(trucktext, 0.0);

        Label firstLabel = new Label("First");
        firstLabel.getStyleClass().add("truck-stext");
        LabelArea.getChildren().add(firstLabel);


        Label lastLabel = new Label("Last");
        lastLabel.getStyleClass().add("truck-stext");
        LabelArea.getChildren().add(lastLabel);

        AnchorPane.setLeftAnchor(lastLabel, 10.0);
        AnchorPane.setBottomAnchor(lastLabel, 0.0);
        AnchorPane.setRightAnchor(firstLabel, 10.0);
        AnchorPane.setBottomAnchor(firstLabel, 0.0);


        AnchorPane truckSlotArea = new AnchorPane();
        AnchorPane.setLeftAnchor(truckSlotArea, 0.0);
        AnchorPane.setRightAnchor(truckSlotArea, 0.0);
        AnchorPane.setBottomAnchor(truckSlotArea, 10.0);
        truckMenu.getChildren().addAll(LabelArea, truckSlotArea);

        HBox slots = new HBox();
        slots.setAlignment(Pos.CENTER);
        slots.setSpacing(10);
        slots.getStyleClass().add("truck-slotsarea");

        AnchorPane.setTopAnchor(slots, 0.0);
        AnchorPane.setBottomAnchor(slots, 0.0);
        AnchorPane.setLeftAnchor(slots, 0.0);
        AnchorPane.setRightAnchor(slots, 0.0);
        truckSlotArea.getChildren().add(slots);

        for (int i = 0; i < 5; i++) {
            StackPane slot = new StackPane();
            slot.setPrefSize(120, 70);
            slot.getStyleClass().add("truck-slot");
            slots.getChildren().add(slot);

            truckSlots[i] = slot;
        }

        ImageView deliveryButton = new ImageView(deliveryBtnImage);
        deliveryButton.getStyleClass().add("truck-button");
        deliveryButton.setFitWidth(80);
        deliveryButton.setFitHeight(80);
        deliveryButton.setCursor(Cursor.HAND);
        AnchorPane.setRightAnchor(deliveryButton, 5.0);
        AnchorPane.setTopAnchor(deliveryButton, -90.0);
        truckMenu.getChildren().add(deliveryButton);

        deliveryButton.setOnMouseClicked(e -> {
            gameController.deliveryParcels(getParcelsInTruck());
            resetRouteHighlight();
        });
    }

    public java.util.List<Parcel> getParcelsInTruck() {
        java.util.List<Parcel> parcels = new java.util.ArrayList<>();

        for (int i = 4; i >= 0; i--) {
            StackPane slot = truckSlots[i];

            if (!slot.getChildren().isEmpty()) {

                javafx.scene.Node parcelNode = slot.getChildren().getFirst();

                if (parcelNode.getUserData() instanceof Parcel) {
                    Parcel p = (Parcel) parcelNode.getUserData();
                    parcels.add(p);
                }
            }
        }

        return parcels;
    }

    public void removeTruckMenu() {
        uiLayer.getChildren().remove(truckMenu);
    }

    private void showStartScreen() {
        startOverlay = new StackPane();
        startOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");

        // ส่วนจัดวางแนวตั้ง
        VBox container = new VBox(30); // 40 คือระยะห่างระหว่าง Logo กับ ปุ่ม
        container.setAlignment(Pos.CENTER);

        // --- ส่วนของ Logo ---
        ImageView logoView = new ImageView(logo);
        logoView.setPreserveRatio(true);
        logoView.setFitWidth(500); // ปรับขนาดความกว้างของ Logo ตามต้องการ
        
        // เพิ่ม Animation เล็กน้อยให้ Logo ดูมีชีวิตชีวา (Fade In)
        FadeTransition logoFade = new FadeTransition(Duration.millis(1000), logoView);
        logoFade.setFromValue(0);
        logoFade.setToValue(1);
        logoFade.play();

        // --- ส่วนของปุ่ม Start ---
        ImageView startBtnView = new ImageView(startbtnImage);
        startBtnView.setPreserveRatio(true);
        startBtnView.setFitWidth(250); // ปรับขนาดปุ่ม Start
        startBtnView.setCursor(Cursor.HAND);

        // เอฟเฟกต์ตอนเอาเมาส์ไปชี้ปุ่ม
        startBtnView.setOnMouseEntered(e -> {
            startBtnView.setScaleX(1.1);
            startBtnView.setScaleY(1.1);
        });
        startBtnView.setOnMouseExited(e -> {
            startBtnView.setScaleX(1.0);
            startBtnView.setScaleY(1.0);
        });

        // เมื่อคลิกปุ่ม Start
        startBtnView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                FadeTransition ft = new FadeTransition(Duration.millis(500), startOverlay);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setOnFinished(event -> {
                    root.getChildren().remove(startOverlay);
                    gameController.start(); // เริ่มระบบเกม
                });
                ft.play();
            }
        });

        // นำ Logo และปุ่มใส่ลงใน VBox
        container.getChildren().addAll(logoView, startBtnView);
        
        // นำ VBox ใส่ลงใน Overlay
        startOverlay.getChildren().add(container);
        
        // นำ Overlay ใส่ลงใน root ของเกม
        root.getChildren().add(startOverlay);
    }

    public void showDailySummary(int parcelDelivered, int income, double distance, int expenses) {
        summaryOverlay = new StackPane();
        summaryOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");

        //gameController.getTimeManager().setPaused(true);
        gameController.getTimeManager().changeTickSpeed(1);

        VBox dialogBox = new VBox(20);
        dialogBox.setAlignment(Pos.CENTER);
        dialogBox.setMaxSize(500, 400);

        Label title = new Label("DAY " + gameController.getTimeManager().getDay());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");

        GridPane statsGrid = new GridPane();
        statsGrid.setAlignment(Pos.CENTER);
        statsGrid.setHgap(30);
        statsGrid.setVgap(15);

        String labelStyle = "-fx-text-fill: lightgray; -fx-font-size: 18px;";

        Label lblDelivered = new Label("Parcels Delivered:");
        lblDelivered.setStyle(labelStyle);
        Label valDelivered = new Label(parcelDelivered + " Boxes");
        valDelivered.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblDistance = new Label("Total Distance:");
        lblDistance.setStyle(labelStyle);
        Label valDistance = new Label(String.format("%.2f km", distance));
        valDistance.setStyle("-fx-text-fill: #facc15; -fx-font-size: 18px; -fx-font-weight: bold;"); // สีเหลือง

        Label lblIncome = new Label("Gross Income:");
        lblIncome.setStyle(labelStyle);
        Label valIncome = new Label("+ ฿ " + String.format("%,d", income));
        valIncome.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 18px; -fx-font-weight: bold;"); // สีเขียว

        Label lblExpenses = new Label("Daily Expenses:");
        lblExpenses.setStyle(labelStyle);
        Label valExpenses = new Label("- ฿ " + String.format("%,d", expenses));
        valExpenses.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 18px; -fx-font-weight: bold;"); // สีแดง

        statsGrid.add(lblDelivered, 0, 0); statsGrid.add(valDelivered, 1, 0);
        //statsGrid.add(lblDistance, 0, 1);  statsGrid.add(valDistance, 1, 1);
        statsGrid.add(lblIncome, 0, 2);    statsGrid.add(valIncome, 1, 2);
        //statsGrid.add(lblExpenses, 0, 3);  statsGrid.add(valExpenses, 1, 3);

        int netProfit = income - expenses;
        HBox netBox = new HBox(20);
        netBox.setAlignment(Pos.CENTER);
        Label lblNet = new Label("NET PROFIT:");
        lblNet.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label valNet = new Label((netProfit >= 0 ? "+ ฿ " : "- ฿ ") + String.format("%,d", Math.abs(netProfit)));
        valNet.setStyle("-fx-text-fill: " + (netProfit >= 0 ? "#4ade80" : "#ef4444") + "; -fx-font-size: 26px; -fx-font-weight: bold;");
        netBox.getChildren().addAll(lblNet, valNet);

        Line separator = new Line(0, 0, 350, 0);
        separator.setStroke(javafx.scene.paint.Color.web("#5a6b7d"));
        separator.setStrokeWidth(2);

        Button btnContinue = new Button("CONTINUE");
        btnContinue.getStyleClass().add("primary-btn");
        btnContinue.setOnAction(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), summaryOverlay);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> root.getChildren().remove(summaryOverlay));
            fadeOut.play();

            gameController.getTimeManager().setPaused(false);
            gameController.saveGame();
        });

        dialogBox.getChildren().addAll(title, statsGrid, separator, netBox, btnContinue);
        summaryOverlay.getChildren().add(dialogBox);

        root.getChildren().add(summaryOverlay);

        summaryOverlay.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(250), summaryOverlay);
        fadeIn.setToValue(1);
        fadeIn.play();

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), dialogBox);
        scaleIn.setFromX(0.7); scaleIn.setFromY(0.7);
        scaleIn.setToX(1.0); scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);
        scaleIn.play();
    }


    public class TruckSprite {
        private StackPane truckWrapper; // 🌟 ใช้ StackPane เพื่อล็อคจุดกึ่งกลางไม่ให้เหวี่ยงตอนหมุน
        private Label percentLabel;
        private ImageView truckImage;
        private double startX, startY, endX, endY;

        public TruckSprite(Province source, Province target) {
            int randomIndex = new java.util.Random().nextInt(TruckImages.length);

            // 🌟 2. นำรูปที่สุ่มได้มาใส่ ImageView
            Image selectedImage = TruckImages[randomIndex];

            if (selectedImage != null) {
                truckImage = new ImageView(selectedImage);
            } else {
                truckImage = new ImageView(); // กันเหนียวกรณีโหลดรูปไม่ติด
            }

            truckImage.setFitWidth(10);
            truckImage.setFitHeight(10);
            truckImage.setPreserveRatio(true);

            percentLabel = new Label("0%");
            percentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: rgba(0,0,0,0.7); -fx-padding: 1 3; -fx-background-radius: 3;");

            // 🌟 2. เลื่อนป้าย % ขึ้นไปด้านบนรถ เพื่อไม่ให้รบกวนจุดศูนย์กลางของการหมุน
            percentLabel.setTranslateY(-20);

            truckWrapper = new StackPane();
            truckWrapper.getChildren().add(truckImage);

            // จัดกึ่งกลางให้อยู่ทับบนเส้นถนนเป๊ะๆ
            truckWrapper.translateXProperty().bind(truckWrapper.widthProperty().divide(-2));
            truckWrapper.translateYProperty().bind(truckWrapper.heightProperty().divide(-2));

            mapGroup.getChildren().add(truckWrapper);

            setRoute(source, target);
        }

        // ฟังก์ชันสั่งให้รถเปลี่ยนจุดหมายไปที่ใหม่
        public void setRoute(Province source, Province target) {
            this.startX = (source.lon - midX) * MAP_SCALE;
            this.startY = (midY - source.lat) * MAP_SCALE;
            this.endX = (target.lon - midX) * MAP_SCALE;
            this.endY = (midY - target.lat) * MAP_SCALE;
            updateProgress(0.0);

            // 🌟 3. ระบบหันหัวรถแบบมุมมองด้านข้าง (ป้องกันล้อชี้ฟ้า)
            double deltaX = this.endX - this.startX;
            double deltaY = this.endY - this.startY;

            if (deltaX >= 0) {
                // กรณีวิ่งไปทางขวา: พลิกรูปให้หันขวา
                truckImage.setScaleX(-1);
                // หามุมเอียง (องศาเทียบกับแกนขวา)
                double tiltAngle = Math.toDegrees(Math.atan2(deltaY, deltaX));
                truckImage.setRotate(tiltAngle);
            } else {
                // กรณีวิ่งไปทางซ้าย: รูปหันซ้ายอยู่แล้ว
                truckImage.setScaleX(1);
                // หามุมเอียง (องศาเทียบกับแกนซ้าย) โดยใช้ -deltaX เป็นบวก
                double tiltAngle = Math.toDegrees(Math.atan2(deltaY, -deltaX));
                truckImage.setRotate(-tiltAngle);
            }
        }

        public void updateProgress(double progress) {
            progress = Math.max(0.0, Math.min(1.0, progress));
            double currentX = startX + (progress * (endX - startX));
            double currentY = startY + (progress * (endY - startY));

            truckWrapper.setLayoutX(currentX);
            truckWrapper.setLayoutY(currentY);
            percentLabel.setText((int)(progress * 100) + "%");
        }

        public void remove() {
            mapGroup.getChildren().remove(truckWrapper);
        }
    }
}