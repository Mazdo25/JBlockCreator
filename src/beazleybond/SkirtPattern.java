package beazleybond;

import jblockmain.*;
import mathcontainers.Vector2D;

import java.util.ArrayList;

/** Class to construct a skirt using the Beazley and Bond drafting method. */
public class SkirtPattern
    extends Pattern
{

    /* Pattern-specific Measurements */
    // In future will be simply extracted from the Measurements object.
    private double a_Waist                     = 70.0;
    private double b_UpperHip                  = 90.0;
    private double c_Hip                       = 96.0;
    private double d_CentreBack                = 60.0;
    private double e_SideSeam                  = 61.0;
    private double f_CentreFront               = 60.0;

    /* Arbitrary Measurements */
    // Some of the following can be inferred from body scan information but for now assume that these follow the
    // empirically driven values.

    // Ensures the waistline drops by 1cm to allow it to curve round the body. This can be informed from the body scan.
    private double Arb_WaistLevel = 1.0;

    // Generic assumption that can in future be informed from the body scan.
    private double Arb_UpperHipLevel = 10.0;

    // Generic assumption that can in future be informed from the body scan.
    private double Arb_HipLevel = 20.0;

    // Waist suppression process required calculation of a front and back dart by dividing up the circumference of the
    // waist. For now we assume a fixed percentage is assigned to each although this could be adjusted in future.
    private double Arb_BackDartPercent = 0.35;
    private double Arb_FrontDartPercent = 0.20;
    private double Arb_SideSeamPercent = 0.45;

    // Dart length is arbitrary but can be inferred from body scan data.
    private double Arb_BackDartLength = 14.0;
    private double Arb_FrontDartLength = 8.0;

    // Dart placement is also arbitrary and is specified as a percentage of quarter waist as measured from the start
    // point of the waist (using strict connectivity order)
    private double Arb_BackDartPlacement = 0.5;
    private double Arb_FrontDartPlacement = 1.0 / 3.0;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Methods */
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public SkirtPattern(Measurements dataStore)
    {
        readMeasurements(dataStore);
        addEasement();
        createBlocks();
    }

    /* Implement abstract methods from super class */
    @Override
    protected void addEasement()
    {
        // Size 12 skirt for now but should be computed by this class in the future.
        a_Waist += 4.0;
        b_UpperHip += 4.0;
        c_Hip += 4.0;
    }

    @Override
    protected void readMeasurements(Measurements dataStore)
    {
        // TODO: Implement when we couple to scan data
    }

    /**
     * The actual block creation process following the drafting method of Beazley and Bond.
     */
    @Override
    protected void createBlocks()
    {
        // Points that make up the shape are listed in a strict anti-clockwise order to maintain correct connectivity for
        // plotting. The bottom left corner of the space to be the origin.

        // Create component representing half back of skirt folded in half.
        blocks.add(new Block("Skirt_Back_Block"));
        Block backBlock = blocks.get(0);

        // Add all the fixed points to the block that coincide with the basic rectangle. These points do not move
        // throughout the drafting process.
        backBlock.addKeypoint(new Vector2D(d_CentreBack, 0.0));
        backBlock.addKeypoint(new Vector2D(d_CentreBack, c_Hip / 4.0));
        backBlock.addKeypoint(new Vector2D(Arb_HipLevel, c_Hip / 4.0));

        // Compute the waistline suppression by finding the difference between the waist measurement and half the hip
        // measurement and then divide by 4 for a quarter distance.
        double Int_WaistSupp = (c_Hip - a_Waist) / 4.0;

        // Add point for waist line drop.
        backBlock.addKeypointNextTo(new Vector2D(Arb_WaistLevel, 0.0),
                                    new Vector2D(d_CentreBack, 0), EPosition.BEFORE);

        // Add point for suppressed side seam at waist.
        // Can be computed using side seam percentage of total suppression required.
        double Int_SuppressedSS = (c_Hip / 4.0) - Arb_SideSeamPercent * Int_WaistSupp;
        backBlock.addKeypointNextTo(new Vector2D(0.0, Int_SuppressedSS),
                                    new Vector2D(Arb_WaistLevel, 0), EPosition.BEFORE);

        // Compute the suppressed Upper Hip Level point.
        // Can be computed from the difference between Hip and Upper Hip
        double Int_SuppressedUpHip = (c_Hip / 4.0) - (c_Hip - b_UpperHip) / 4.0;

        // Add curve between waist point and hip point (rather than upper-hip as stipulated in BB).
        // Assume for now, in the absence of vary form curve that this is a curve defined by a circle.
        backBlock.addCircularCurve(new Vector2D(Arb_HipLevel, c_Hip / 4.0),
                                   new Vector2D(0.0, Int_SuppressedSS), 0.5, true);

        // Trace off block
        blocks.add(new Block(backBlock, "Skirt_Front_Block"));
        Block frontBlock = blocks.get(blocks.size()- 1);

        // Add back dart.
        ArrayList<Vector2D> dartEdges = backBlock.addDart(new Vector2D(0.0, Int_SuppressedSS),
                                                          new Vector2D(Arb_WaistLevel, 0.0),
                                                          Arb_BackDartPlacement,
                                                          Arb_BackDartPercent * Int_WaistSupp,
                                                          Arb_BackDartLength);

        // Add curves either side of dart ensuring the curve intersects the joining edges at a right angle.
        backBlock.addRightAngleCurve(new Vector2D(0.0, Int_SuppressedSS), dartEdges.get(0));

        backBlock.addRightAngleCurve(dartEdges.get(2), new Vector2D(Arb_WaistLevel, 0.0));

        // Add front dart
        dartEdges = frontBlock.addDart(new Vector2D(0.0, Int_SuppressedSS),
                                       new Vector2D(Arb_WaistLevel, 0.0),
                                       Arb_FrontDartPlacement,
                                       Arb_FrontDartPercent * Int_WaistSupp,
                                       Arb_FrontDartLength);

        // Add curves
        frontBlock.addRightAngleCurve(new Vector2D(0.0, Int_SuppressedSS), dartEdges.get(0));

        frontBlock.addRightAngleCurve(dartEdges.get(2), new Vector2D(Arb_WaistLevel, 0.0));
    }
}
