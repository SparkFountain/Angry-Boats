AppTitle("Angry Boats")
Graphics3D(800,600,32,2)
SetBuffer(BackBuffer())
Global frameTimer = CreateTimer(60)
SeedRnd(MilliSecs())

Global cam = CreateCamera()
CameraClsColor(cam,63,230,243)
PositionEntity(cam,-100,10,0)
Global camPitch#, camYaw#, camRoll#
Global light = CreateLight()
AmbientLight(160,160,160)

;plane is the water
Global water = CreatePlane()
Global waterTexture = LoadTexture("water.jpg",8)
ScaleTexture(waterTexture,15,15)
EntityTexture(water,waterTexture)
EntityAlpha(water,0.8)

;surrounding terrain
Global terrain
Global terrainTexture = LoadTexture("beach.jpg") : ScaleTexture(terrainTexture,20,20)


Global gameSection$ = "menu"
Global level = 1
Global loadLevel = True
Global moving = False	;whether boat is moving or player can still position it
Global drift#			;drift (in z direction)

Global dirHelp = CreateCube():ScaleEntity(dirHelp,100,0.1,0.1)

;resources
Global titleScreen = LoadSprite("title.jpg",4)
PositionEntity(titleScreen,-100,10,1.7)
Global done = LoadSprite("done.jpg",4)
PositionEntity(done,-200,10,1.7)
Dim boats(4)
boats(0) = LoadAnimMesh("0.b3d"):ScaleEntity(boats(0),0.3,0.3,0.3):RotateEntity(boats(0),0,270,0):PositionEntity(boats(0),0,1,0):HideEntity(boats(0))
boats(1) = LoadAnimMesh("1.b3d"):ScaleEntity(boats(1),0.1,0.1,0.1):RotateEntity(boats(1),0,270,0):HideEntity(boats(1))
boats(2) = LoadAnimMesh("2.b3d"):ScaleEntity(boats(2),0.1,0.1,0.1):RotateEntity(boats(2),0,270,0):HideEntity(boats(2))
boats(3) = LoadAnimMesh("3.b3d"):ScaleEntity(boats(3),0.1,0.1,0.1):RotateEntity(boats(3),0,270,0):HideEntity(boats(3))


Dim boatSpeed#(3):boatSpeed(0)=0.01:boatSpeed(1)=0.013:boatSpeed(2)=0.017:boatSpeed(3)=0.02
Dim boatDrift(3):boatDrift(0)=6000:boatDrift(1)=10000:boatDrift(2)=4000:boatDrift(3)=14000
Dim tourists(3)
For j=0 To 3
	tourists(j) = LoadMesh("t"+j+".b3d")
	ScaleEntity(tourists(j),0.1,0.1,0.1)
	HideEntity(tourists(j))
Next

;create start line: is always there
For k=-50 To 50
	c=CreateCube():ScaleEntity(c,0.1,0.1,0.5):PositionEntity(c,0,0,k):EntityTexture(c,LoadTexture("startLine.jpg"))
Next

;arrow for drift direction
Global arrow = LoadMesh("a.b3d")
EntityColor(arrow,160,0,20)
ScaleEntity(arrow,0.07,0.07,0.07)
PositionEntity(arrow,-1,21,12)


;DEBUG STUFF
Global msx, msy


;MAIN LOOP
While Not KeyHit(1)
	
	msx = MouseXSpeed() : msy = MouseYSpeed()
	
	Cls()
	
	;DEBUG
	DebugMove()
	
	Select gameSection
		Case "menu"
			If(KeyHit(28)) Then gameSection="ingame"
		Case "ingame"
			If(loadLevel Or KeyHit(14)) Then
				LoadLevelStuff()
				loadLevel = False
			EndIf
			
			If(moving) Then
				;show boat moving animation
				MoveBoat()
				CheckCollisions()
				Sinking()
			Else
				ControlBoat()
			EndIf
			MoveWater()
			
			If(KeyHit(68)) Then
				;skip level
				level=level+1
				If(level>6) Then
					gameSection="done"
				Else
					LoadLevelStuff()
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
	
	;turn angle
	If(KeyDown(203) Or KeyDown(205)) Then
		For b.Boat = Each Boat
			If(b\active) Then
				If(KeyDown(203) And EntityYaw(b\mesh)<-30) Then TurnEntity(b\mesh,0,0.1,0):TurnEntity(dirHelp,0,0.1,0)
				If(KeyDown(205) And EntityYaw(b\mesh)>-160) Then TurnEntity(b\mesh,0,-0.1,0):TurnEntity(dirHelp,0,-0.1,0)
				Exit
			EndIf
		Next
	EndIf
	
	;drag boat
	If(KeyDown(200) Or KeyDown(208)) Then
		For b.Boat = Each Boat
			If(b\active) Then
				If(KeyDown(200) And EntityZ(b\mesh)<16) Then PositionEntity(b\mesh,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh)+0.02):PositionEntity(dirHelp,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh)+0.02)
				If(KeyDown(208) And EntityZ(b\mesh)>-16) Then PositionEntity(b\mesh,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh)-0.02):PositionEntity(dirHelp,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh)-0.02)
			EndIf
		Next
	EndIf
	
	;show help
	If(KeyDown(54)) Then
		ShowEntity(dirHelp)
	Else
		HideEntity(dirHelp)
	EndIf
	
	;letz go!
	If(KeyHit(57)) Then
		HideEntity(arrow):HideEntity(dirHelp)
		
		;re-position the cam
		For b.Boat = Each Boat
			If(b\active) Then
				PositionEntity(cam,-10,5,0)
				RotateEntity(cam,0,-90,0)
				EntityParent(cam,b\mesh)
			EndIf
		Next
		
		moving = True
	EndIf
	
End Function



Function MoveBoat()
	
	For b.Boat = Each Boat
		If(b\active) Then
			If(b\alive=False) Then 
				MoveEntity(b\mesh,0,-0.003,0) : TurnEntity(b\mesh,0,Rnd(0,0.1),0)
				If(EntityY(b\mesh)<-3) Then EntityParent(cam,0) : FreeEntity(b\mesh) : Delete b : ActivateNextBoat()
			Else
				MoveEntity(b\mesh,0,0,boatSpeed(b\sort))
				PositionEntity(b\mesh,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh)+(Float(drift)/boatDrift(b\sort)))
				If(Not(Animating(b\mesh))) Then
					Select b\sort
						Case 0 : Animate(b\mesh,2,0.07)
						Case 1 : Animate(b\mesh,2,0.25)
					End Select
				EndIf
			EndIf
			Exit
		EndIf
	Next
	
End Function



Function ActivateNextBoat()
	
	;check if there are still tourists
	t.Tourist = First Tourist
	If(t=Null) Then 
		level=level+1
		If(level>6) Then gameSection="done" : Return
		LoadLevelStuff()	;this happens anyway, either repeat this level or continue with next
	EndIf
	
	moving = False
	b.Boat = First Boat
	If(b=Null) Then LoadLevelStuff() : Return
	b\active = True
	ShowEntity(b\mesh)
	ShowEntity(arrow)
	;re-position the cam
	PositionEntity(cam,16,40,0)
	RotateEntity(cam,90,0,0)
	;reset helper
	FreeEntity(dirHelp)
	dirHelp = CreateCube():ScaleEntity(dirHelp,100,0.1,0.1)
	
End Function



Function CheckCollisions()
	
	For b.Boat = Each Boat
		If(b\active) Then
			;boat <-> tourist
			For t.Tourist = Each Tourist
				If(MeshesIntersect(b\mesh,t\mesh)) Then
					t\alive = False		;sink, tourist^^
				EndIf
			Next
			;boat <-> terrain
			If(TerrainY(terrain,EntityX(b\mesh),EntityY(b\mesh),EntityZ(b\mesh))>-1) Then
				b\alive=False
			EndIf
		EndIf
	Next
	
End Function



Function Sinking()
	
	For t.Tourist = Each Tourist
		If(t\alive=False) Then
			PositionEntity(t\mesh,EntityX(t\mesh),EntityY(t\mesh)-0.01,EntityZ(t\mesh))
			If(EntityY(t\mesh)<-3) Then FreeEntity(t\mesh):Delete t
		EndIf
	Next
	
End Function



Function LoadLevelStuff()
	
	;hide sprite from menu
	HideEntity(titleScreen)
	
	;re-position the cam
	PositionEntity(cam,16,40,0)
	RotateEntity(cam,90,0,0)
	
	;delete old content
	For b.Boat = Each Boat
		FreeEntity(b\mesh)
		Delete b
	Next
	For t.Tourist = Each Tourist
		FreeEntity(t\mesh)
		Delete t
	Next
	FreeEntity(terrain)
	
	;reset states
	moving = False
	
	;select level -> definitions are made right here!
	terrain=LoadTerrain("terrain"+level+".jpg") : ScaleEntity(terrain,2,5,2) : PositionEntity(terrain,-30,-2,-65) : EntityTexture(terrain,terrainTexture)
	Select level
		Case 1
			drift=0
			CreateBoat(0,1):CreateBoat(0):CreateBoat(0):CreateBoat(0):CreateBoat(0)
			CreateTourist(35,6,0):CreateTourist(40,4,0):CreateTourist(45,2,0)
			CreateTourist(35,-6,0):CreateTourist(40,-4,0):CreateTourist(45,-2,0):CreateTourist(50,0,3)
			CreateTourist(20,0,0):CreateTourist(25,0,0):CreateTourist(30,0,0):CreateTourist(35,0,0):CreateTourist(40,0,0):CreateTourist(45,0,0)
		Case 2
			drift=10
			CreateBoat(0,1):CreateBoat(0):CreateBoat(0):CreateBoat(0):CreateBoat(0)
			CreateTourist(15,6,0):CreateTourist(20,3,0):CreateTourist(30,-3,0):CreateTourist(35,-6,0)
			CreateTourist(15,-6,0):CreateTourist(20,-3,0):CreateTourist(30,3,0):CreateTourist(35,6,0):CreateTourist(25,0,2)
		Case 3
			drift=50
			CreateBoat(1,1):CreateBoat(1):CreateBoat(1):CreateBoat(1)
			CreateTourist(38,0,1):CreateTourist(32,7,1):CreateTourist(24,11,1):CreateTourist(32,-7,1):CreateTourist(24,-11,1)
			CreateTourist(10,-8,3):CreateTourist(10,8,3)
		Case 4
			drift=-25
			CreateBoat(2,1):CreateBoat(2):CreateBoat(0):CreateBoat(2)
			CreateTourist(20,7,0):CreateTourist(24,4,1):CreateTourist(26,-2,0):CreateTourist(20,-12,0):CreateTourist(24,-12,1):CreateTourist(26,-8,0)
			CreateTourist(40,-5,2):CreateTourist(20,-25,2)
		Case 5
			drift=-45
			CreateBoat(2,1):CreateBoat(1):CreateBoat(2):CreateBoat(3)
			CreateTourist(15,0,0):CreateTourist(20,0,0):CreateTourist(25,0,0):CreateTourist(30,0,0):CreateTourist(35,0,0):CreateTourist(40,0,0)
			CreateTourist(45,-10,3):CreateTourist(45,-5,3):CreateTourist(45,5,3):CreateTourist(45,10,3)
		Case 6
			drift=20
			CreateBoat(1,1):CreateBoat(1):CreateBoat(3)
			CreateTourist(8,1,3):CreateTourist(8,-6,3):CreateTourist(17,-2,2):CreateTourist(19,-15,1)
			CreateTourist(47,0,0)
	End Select
	RotateEntity(arrow,0,drift,0)
	
End Function



Function CreateBoat(sort, active=False)
	
	Local b.Boat = New Boat
	b\alive = True
	b\active = active
	b\sort = sort
	b\mesh = CopyEntity(boats(sort))
	If(Not(active)) Then HideEntity(b\mesh)
	
End Function



Function CreateTourist(x#, z#, sort)
	
	Local t.Tourist = New Tourist
	t\alive = True
	t\mesh = CopyEntity(tourists(sort))
	PositionEntity(t\mesh,x,0,z)
	ShowEntity(t\mesh)
	
End Function



Function MoveWater()
	
	PositionEntity(water,Sin(MilliSecs()/20),0,Sin(MilliSecs()/30))
	
End Function



Function DebugMove()
	
	If KeyDown(17) Then MoveEntity cam,0,0,0.1
	If KeyDown(31) Then MoveEntity cam,0,0,-0.1
	If KeyDown(30) Then MoveEntity cam,-0.1,0,0
	If KeyDown(32) Then MoveEntity cam,0.1,0,0
	
	;camera yaw
	If((msx <> 0) And (MouseDown(2))) Then
		If(msx > 0) Then
			camYaw=camYaw-0.5
			RotateEntity cam,camPitch,camYaw,camRoll
		Else
			camYaw=camYaw+0.5
			RotateEntity cam,camPitch,camYaw,camRoll
		EndIf
	EndIf 
	
	;camera pitch
	If((msy <> 0) And (MouseDown(2))) Then
		If((msy > 0) And (camPitch<90)) Then
			camPitch=camPitch+0.5
			RotateEntity cam,camPitch,camYaw,camRoll
		ElseIf((msy < 0) And (camPitch>-90)) Then
			camPitch=camPitch-0.5
			RotateEntity cam,camPitch,camYaw,camRoll
		EndIf
	EndIf
	
End Function



Type Boat
	Field active	;whether player can move this boat or not
	Field alive		;if not, it sinks
	Field sort		;important for speed and special action
	Field mesh
End Type


Type Tourist
	Field alive		;if not, he is sinking down
	Field mesh
End Type
;~IDEal Editor Parameters:
;~F#145#15E#166#18F
;~C#Blitz3D