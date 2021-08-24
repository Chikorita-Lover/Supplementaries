package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class LOD {
    private final double distSq;

    public LOD(double distance){
        this.distSq = distance;
    }

    public LOD(TileEntityRendererDispatcher renderer, BlockPos pos){
        this(renderer.camera.getPosition(),pos);
    }
    public LOD(Vector3d cameraPos, BlockPos pos){
        this(Vector3d.atCenterOf(pos).distanceToSqr(cameraPos));
    }

    public boolean isOnEdge(TileEntity te){return this.distSq >(te.getViewDistance()*te.getViewDistance())-BUFFER;}
    public boolean isNear(){
        return this.distSq <NEAR_DIST;
    }
    public boolean isNearMed(){
        return this.distSq <NEAR_MED_DIST;
    }
    public boolean isMedium(){
        return this.distSq <MEDIUM_DIST;
    }
    public boolean isFar(){
        return this.distSq <FAR_DIST;
    }


    //all squared
    public static final int BUFFER = 2*2;
    public static final int NEAR_DIST = 32*32;
    public static final int NEAR_MED_DIST = 48*48;
    public static final int MEDIUM_DIST = 64*64;
    public static final int FAR_DIST = 96*96;

    public static boolean isOutOfFocus(Vector3d cameraPos, BlockPos pos, float blockYaw){
        float relAngle = (float) (MathHelper.atan2(cameraPos.x-(pos.getX()+0.5f),cameraPos.z-(pos.getZ()+0.5f))*180/Math.PI);
        return(MathHelper.degreesDifference(relAngle,blockYaw-90)>-0);
    }
    public static boolean isOutOfFocus(Vector3d cameraPos, BlockPos pos, float blockYaw, Direction dir, float offset){
        float relAngle = (float) (MathHelper.atan2(
                offset*dir.getStepX()+cameraPos.x-(pos.getX()+0.5f),
                offset*dir.getStepZ()+cameraPos.z-(pos.getZ()+0.5f))*180/Math.PI);
        return(MathHelper.degreesDifference(relAngle,blockYaw-90)>-0);
    }


}