AppTitle("Angry Boats")
Graphics3D(800,600,32,2)
SetBuffer(BackBuffer())
Global fT=CreateTimer(60)
SeedRnd(MilliSecs())
Global cam=CreateCamera()
CameraClsColor(cam,63,230,243)
PositionEntity(cam,-100,10,0)
Global light=CreateLight()
AmbientLight(160,160,160)
Global water=CreatePlane()
Global wT=LoadTexture("water.jpg",8)
ScaleTexture(wT,15,15)
EntityTexture(water,wT)
EntityAlpha(water,0.8)
Global terrain
Global tTe=LoadTexture("beach.jpg"):ScaleTexture(tTe,20,20)
Global gameSection$="menu"
Global level=1
Global loadLevel=True
Global moving=False
Global drift#
Global dH=CreateCube():ScaleEntity(dH,100,0.1,0.1)
Global titleScreen=LoadSprite("title.jpg",4)
PositionEntity(titleScreen,-100,10,1.7)
Global done=LoadSprite("done.jpg",4)
PositionEntity(done,-200,10,1.7)
Dim f(4)
f(0)=LoadAnimMesh("0.b3d"):ScaleEntity(f(0),0.3,0.3,0.3):RotateEntity(f(0),0,270,0):PositionEntity(f(0),0,1,0):HideEntity(f(0))
f(1)=LoadAnimMesh("1.b3d"):ScaleEntity(f(1),0.1,0.1,0.1):RotateEntity(f(1),0,270,0):HideEntity(f(1))
f(2)=LoadAnimMesh("2.b3d"):ScaleEntity(f(2),0.1,0.1,0.1):RotateEntity(f(2),0,270,0):HideEntity(f(2))
f(3)=LoadAnimMesh("3.b3d"):ScaleEntity(f(3),0.1,0.1,0.1):RotateEntity(f(3),0,270,0):HideEntity(f(3))
Dim bSp#(3):bSp(0)=0.01:bSp(1)=0.013:bSp(2)=0.017:bSp(3)=0.02
Dim boatDrift(3):boatDrift(0)=6000:boatDrift(1)=10000:boatDrift(2)=4000:boatDrift(3)=14000
Dim ts(3)
For j=0 To 3
ts(j)=LoadMesh("t"+j+".b3d")
ScaleEntity(ts(j),0.1,0.1,0.1)
HideEntity(ts(j))
Next
For k=-50 To 50
c=CreateCube():ScaleEntity(c,0.1,0.1,0.5):PositionEntity(c,0,0,k):EntityTexture(c,LoadTexture("startLine.jpg"))
Next
Global arrow=LoadMesh("a.b3d")
EntityColor(arrow,160,0,20)
ScaleEntity(arrow,0.07,0.07,0.07)
PositionEntity(arrow,-1,21,12)
Global msx, msy
While Not KeyHit(1)
Cls()
Select gameSection
Case "menu"
If(KeyHit(28)) Then gameSection="ingame"
Case "ingame"
If(loadLevel Or KeyHit(14)) Then
LLS()
loadLevel=False
EndIf
If(moving) Then
MB()
CheckCollisions()
SK()
Else
ControlBoat()
EndIf
MoveWater()
If(KeyHit(68)) Then
level=level+1
If(level>6) Then
gameSection="done"
Else
LLS()
EndIf
EndIf
Case "done"
PositionEntity(cam,-200,10,0)
RotateEntity(cam,0,0,0)
ShowEntity(done)
End Select
UpdateWorld()
RenderWorld()
Flip(0)
Wend
End
Function ControlBoat()
If(KeyDown(203) Or KeyDown(205)) Then
For b1.B=Each B
If(b1\a) Then
If(KeyDown(203) And EntityYaw(b1\m)<-30) Then TurnEntity(b1\m,0,0.1,0):TurnEntity(dH,0,0.1,0)
If(KeyDown(205) And EntityYaw(b1\m)>-160) Then TurnEntity(b1\m,0,-0.1,0):TurnEntity(dH,0,-0.1,0)
Exit
EndIf
Next
EndIf
If(KeyDown(200) Or KeyDown(208)) Then
For b1.B=Each B
If(b1\a) Then
If(KeyDown(200) And EntityZ(b1\m)<16) Then PositionEntity(b1\m,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m)+0.02):PositionEntity(dH,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m)+0.02)
If(KeyDown(208) And EntityZ(b1\m)>-16) Then PositionEntity(b1\m,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m)-0.02):PositionEntity(dH,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m)-0.02)
EndIf
Next
EndIf
If(KeyDown(54)) Then
ShowEntity(dH)
Else
HideEntity(dH)
EndIf
If(KeyHit(57)) Then
HideEntity(arrow):HideEntity(dH)
For b1.B=Each B
If(b1\a) Then
PositionEntity(cam,-10,5,0)
RotateEntity(cam,0,-90,0)
EntityParent(cam,b1\m)
EndIf
Next
moving=True
EndIf
End Function
Function MB()
For b1.B=Each B
If(b1\a) Then
If(b1\b=False) Then 
MoveEntity(b1\m,0,-0.003,0):TurnEntity(b1\m,0,Rnd(0,0.1),0)
If(EntityY(b1\m)<-3) Then EntityParent(cam,0):FreeEntity(b1\m):Delete b1:ActivateNextBoat()
Else
MoveEntity(b1\m,0,0,bSp(b1\sort))
PositionEntity(b1\m,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m)+(Float(drift)/boatDrift(b1\sort)))
If(Not(Animating(b1\m))) Then
Select b1\sort
Case 0:Animate(b1\m,2,0.07)
Case 1:Animate(b1\m,2,0.25)
End Select
EndIf
EndIf
Exit
EndIf
Next
End Function
Function ActivateNextBoat()
t.T=First T
If(t=Null) Then 
level=level+1
If(level>6) Then gameSection="done":Return
LLS()
EndIf
moving=False
b1.B=First B
If(b1=Null) Then LLS():Return
b1\a=True
ShowEntity(b1\m)
ShowEntity(arrow)
PositionEntity(cam,16,40,0)
RotateEntity(cam,90,0,0)
FreeEntity(dH)
dH=CreateCube():ScaleEntity(dH,100,0.1,0.1)
End Function
Function CheckCollisions()
For b1.B=Each B
If(b1\a) Then
For t1.T=Each T
If(MeshesIntersect(b1\m,t1\m)) Then
t1\a=False
EndIf
Next
If(TerrainY(terrain,EntityX(b1\m),EntityY(b1\m),EntityZ(b1\m))>-1) Then
b1\b=False
EndIf
EndIf
Next
End Function
Function SK()
For t1.T=Each T
If(t1\a=False) Then
PositionEntity(t1\m,EntityX(t1\m),EntityY(t1\m)-0.01,EntityZ(t1\m))
If(EntityY(t1\m)<-3) Then FreeEntity(t1\m):Delete t1
EndIf
Next
End Function
Function LLS()
HideEntity(titleScreen)
PositionEntity(cam,16,40,0)
RotateEntity(cam,90,0,0)
For b1.B=Each B
FreeEntity(b1\m)
Delete b1
Next
For t1.T=Each T
FreeEntity(t1\m)
Delete t1
Next
FreeEntity(terrain)
moving=False
terrain=LoadTerrain("terrain"+level+".jpg"):ScaleEntity(terrain,2,5,2):PositionEntity(terrain,-30,-2,-65):EntityTexture(terrain,tTe)
Select level
Case 1
drift=0
CB(0,1):CB(0):CB(0):CB(0):CB(0)
CT(35,6,0):CT(40,4,0):CT(45,2,0)
CT(35,-6,0):CT(40,-4,0):CT(45,-2,0):CT(50,0,3)
CT(20,0,0):CT(25,0,0):CT(30,0,0):CT(35,0,0):CT(40,0,0):CT(45,0,0)
Case 2
drift=10
CB(0,1):CB(0):CB(0):CB(0):CB(0)
CT(15,6,0):CT(20,3,0):CT(30,-3,0):CT(35,-6,0)
CT(15,-6,0):CT(20,-3,0):CT(30,3,0):CT(35,6,0):CT(25,0,2)
Case 3
drift=50
CB(1,1):CB(1):CB(1):CB(1)
CT(38,0,1):CT(32,7,1):CT(24,11,1):CT(32,-7,1):CT(24,-11,1)
CT(10,-8,3):CT(10,8,3)
End Select
RotateEntity(arrow,0,drift,0)
End Function
Function CB(sort, active=False)
Local b1.B=New B
b1\b=True
b1\a=active
b1\sort=sort
b1\m=CopyEntity(f(sort))
If(Not(active)) Then HideEntity(b1\m)
End Function
Function CT(x#, z#, sort)
Local t1.T=New T
t1\a=True
t1\m=CopyEntity(ts(sort))
PositionEntity(t1\m,x,0,z)
ShowEntity(t1\m)
End Function
Function MoveWater()
PositionEntity(water,Sin(MilliSecs()/20),0,Sin(MilliSecs()/30))
End Function
Type B
Field a
Field b
Field sort
Field m
End Type
Type T
Field a
Field m
End Type