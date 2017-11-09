package com.github.nabezokodaikon.pcars2

import spray.json._
import DefaultJsonProtocol._

object UdpDataJsonProtocol extends DefaultJsonProtocol {
  implicit val packetBaseFormat = jsonFormat8(PacketBase)
  implicit val telemetryParticipantInfoFormat = jsonFormat1(TelemetryParticipantInfo)
  implicit val unfilteredInputFormat = jsonFormat4(UnfilteredInput)
  implicit val carStateFormat = jsonFormat20(CarState)
  implicit val velocityFormat = jsonFormat7(Velocity)
  implicit val tyre1Format = jsonFormat22(Tyre1)
  implicit val tyre2Format = jsonFormat2(Tyre2)
  implicit val tyre3Format = jsonFormat4(Tyre3)
  implicit val carDamageFormat = jsonFormat2(CarDamage)
  implicit val hwStateFormat = jsonFormat3(HWState)
  implicit val telemetryDataFormat = jsonFormat10(TelemetryData)
  implicit val raceDataFormat = jsonFormat19(RaceData)
  implicit val participantsDataFormat = jsonFormat3(ParticipantsData)
  implicit val participantInfoFormat = jsonFormat13(ParticipantInfo)
  implicit val formatParticipantInfoFormat = jsonFormat13(FormatParticipantInfo)
  implicit val timingsDataFormat = jsonFormat9(TimingsData)
  implicit val gameStateFormat = jsonFormat2(GameState)
  implicit val gameSessionStateFormat = jsonFormat2(SessionState)
  implicit val gameStateDataFormat = jsonFormat11(GameStateData)
  implicit val participantStatsInfoFormat = jsonFormat6(ParticipantStatsInfo)
  implicit val formatParticipantStatsInfoFormat = jsonFormat6(FormatParticipantStatsInfo)
  implicit val participantsStatsFormat = jsonFormat2(ParticipantsStats)
  implicit val timeStatsDataFormat = jsonFormat3(TimeStatsData)
  implicit val vehicleInfoFormat = jsonFormat3(VehicleInfo)
  implicit val participantVehicleNamesDataFormat = jsonFormat2(ParticipantVehicleNamesData)
  implicit val classInfoFormat = jsonFormat2(ClassInfo)
  implicit val vehicleClassNamesDataFormat = jsonFormat2(VehicleClassNamesData)

  implicit val lapTimeFormat = jsonFormat5(LapTime)
  implicit val lapTimeDetailsFormat = jsonFormat7(LapTimeDetails)
}

import UdpDataJsonProtocol._

object UdpStreamerPacketHandlerType {
  val CAR_PHYSICS: Byte = 0 // TelemetryData
  val RACE_DEFINITION: Byte = 1 // RaceData
  val PARTICIPANTS: Byte = 2 // ParticipantsData
  val TIMINGS: Byte = 3 // TimingsData
  val GAME_STATE: Byte = 4 // GameStateData
  val WEATHER_STATE: Byte = 5 // not sent at the moment, information can be found in the game state packet
  val VEHICLE_NAMES: Byte = 6 // not sent at the moment
  val TIME_STATS: Byte = 7 // TimeStatsData
  val PARTICIPANT_VEHICLE_NAMES: Byte = 8 // ParticipantVehicleNamesData or VehicleClassNamesData

  /*
   * Original data
   */
  val LAP_TIME_DETAILS: Byte = 64
}

object PacketSize {
  val TELEMETRY_DATA: Short = 538
  val RACE_DATA: Short = 308
  val PARTICIPANTS_DATA: Short = 1040
  val TIMINGS_DATA: Short = 993
  val GAME_STATE_DATA: Short = 24
  val TIME_STATS_DATA: Short = 784
  val PARTICIPANT_VEHICLE_NAMES_DATA: Short = 1164
  val VEHICLE_CLASS_NAMES_DATA: Short = 1452
}

object UdpDataConst {
  val UDP_STREAMER_PARTICIPANTS_SUPPORTED: Byte = 32
  val UDP_STREAMER_CAR_PHYSICS_HANDLER_VERSION: Byte = 2
  val TYRE_NAME_LENGTH_MAX: Byte = 40
  val TRACKNAME_LENGTH_MAX: Byte = 64
  val PARTICIPANTS_PER_PACKET: Byte = 16
  val PARTICIPANT_NAME_LENGTH_MAX: Byte = 64
  val VEHICLE_NAME_LENGTH_MAX: Byte = 64
  val VEHICLES_PER_PACKET: Byte = 16
  val CLASS_NAME_LENGTH_MAX: Byte = 20
  val CLASSES_SUPPORTED_PER_PACKET: Byte = 60

  val GEAR_NEUTRAL: String = "N"
  val GEAR_REVERS: String = "R"
}

trait UdpData {
  val base: PacketBase
  def toJsonString(): String
}

/*
 * Each packet holds a base header with identification info to help with UDP unreliability.
 */
case class PacketBase(
    packetNumber: Long, // 0 counter reflecting all the packets that have been sent during the game run
    categoryPacketNumber: Long, // 4 counter of the packet groups belonging to the given category
    partialPacketIndex: Short, // 8 If the data from this class had to be sent in several packets, the index number
    partialPacketNumber: Short, // 9 If the data from this class had to be sent in several packets, the total number
    packetType: Short, // 10 what is the type of this packet (see EUDPStreamerPacketHanlderType for details)
    packetVersion: Short, // 11 what is the version of protocol for this handler, to be bumped with data structure change
    dataTimestamp: Long,
    dataSize: Short
)

/*******************************************************************************************************************
//
//	Telemetry data for the viewed participant. 
//
//	Frequency: Each tick of the UDP streamer how it is set in the options
//	When it is sent: in race
//
*******************************************************************************************************************/
case class TelemetryParticipantInfo(
    viewedParticipantIndex: Byte
)

case class UnfilteredInput(
    unfilteredThrottle: Short, // 0 - 255
    unfilteredBrake: Short, // 0 - 255
    unfilteredSteering: Byte, // -127(Left) - +127(Right)
    unfilteredClutch: Short // 0 - 255
)

case class CarState(
    carFlags: Short,
    oilTempCelsius: Float, // [ Unit: Celsius ] [ value / 255f ]
    oilPressureKPa: Int,
    waterTempCelsius: Float, // [ Unit: Celsius ] [ value / 255f ]
    waterPressureKpa: Int,
    fuelPressureKpa: Int,
    fuelCapacity: Short, // [ Unit: liter ]
    brake: Short, // [ 0 - 255 ]
    throttle: Short, // [ 0 - 255 ]
    clutch: Short, // [ 0 - 255 ]
    fuelLevel: Float,
    speed: Float, // [ Unit: KM/H ] [ value * 3.6f ]
    rpm: Int,
    maxRpm: Int,
    steering: Byte, // [ -127(Left) - +127(Right) ]
    gear: String,
    numGears: Byte,
    boostAmount: Short,
    crashState: Short,
    odometerKM: Float
)

case class Velocity(
    orientation: Array[Float],
    localVelocity: Array[Float],
    worldVelocity: Array[Float],
    angularVelocity: Array[Float],
    localAcceleration: Array[Float],
    worldAcceleration: Array[Float],
    extentsCentre: Array[Float]
)

case class Tyre1(
    tyreFlags: Array[Short],
    terrain: Array[Short],
    tyreY: Array[Float],
    tyreRPS: Array[Float],
    tyreTemp: Array[Short],
    tyreHeightAboveGround: Array[Float],
    tyreWear: Array[Short],
    brakeDamage: Array[Short],
    suspensionDamage: Array[Short],
    brakeTempCelsius: Array[Short], // [ Unit: Celsius ] [ Convert: value / 255 ]
    tyreTreadTemp: Array[Int],
    tyreLayerTemp: Array[Int],
    tyreCarcassTemp: Array[Int],
    tyreRimTemp: Array[Int],
    tyreInternalAirTemp: Array[Int],
    tyreTempLeft: Array[Int], // [ Unit: Celsius ]
    tyreTempCenter: Array[Int], // [ Unit: Celsius ]
    tyreTempRight: Array[Int], // [ Unit: Celsius ]
    wheelLocalPositionY: Array[Float],
    rideHeight: Array[Float], // [ Unit: Metric ]
    suspensionTravel: Array[Float], // [ Unit: Metric ]
    suspensionVelocity: Array[Float]
)
case class Tyre2(
    suspensionRideHeight: Array[Int],
    airPressure: Array[Int] // [ Unit: bar ] [ Convert: value / 10 ]
)

case class Tyre3(
    engineSpeed: Float,
    engineTorque: Float,
    wings: Array[Short],
    handBrake: Short
)

case class CarDamage(
    aeroDamage: Short,
    engineDamage: Short
)

case class HWState(
    joyPad0: Long,
    dPad: Short,
    tyreCompound: Array[String]
)

// partialPacketNumber = 1 Only
case class TelemetryData(
    base: PacketBase,
    participantinfo: TelemetryParticipantInfo,
    unfilteredInput: UnfilteredInput,
    carState: CarState,
    velocity: Velocity,
    tyre1: Tyre1,
    tyre2: Tyre2,
    tyre3: Tyre3,
    carDamage: CarDamage,
    hwState: HWState
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Race stats data.  
//
//	Frequency: Logaritmic decrease
//	When it is sent: Counter resets on entering InRace state and again each time any of the values changes
//
*******************************************************************************************************************/
// partialPacketNumber = 1 Only
case class RaceData(
    base: PacketBase,
    worldFastestLapTime: String, // [ Unit: Seconds ]
    personalFastestLapTime: String, // [ Unit: Seconds ]
    personalFastestSector1Time: String, // [ Unit: Seconds ]
    personalFastestSector2Time: String, // [ Unit: Seconds ]
    personalFastestSector3Time: String, // [ Unit: Seconds ]
    worldFastestSector1Time: String, // [ Unit: Seconds ]
    worldFastestSector2Time: String, // [ Unit: Seconds ]
    worldFastestSector3Time: String, // [ Unit: Seconds ]
    trackLength: Float,
    trackLocation: String,
    trackVariation: String,
    translatedTrackLocation: String,
    translatedTrackVariation: String,
    lapsTimeInEvent: Int, // contains lap number for lap based session or quantized session duration (number of 5mins) for timed sessions, the top bit is 1 for timed sessions TODO: セッション時間の場合、常に値が 65535 になる。
    isTimedSessions: Boolean,
    lapsInEvent: Int,
    sessionLengthTimeInEvent: Int,
    enforcedPitStopLap: Byte
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Participant names data.  
//
//	Frequency: Logarithmic decrease
//	When it is sent: Counter resets on entering InRace state and again each  the participants change. 
//	The sParticipantsChangedTimestamp represent last time the participants has changed andis  to be used to sync 
//	this information with the rest of the participant related packets
//
*******************************************************************************************************************/
// partialPacketNumber = 1 or 2
case class ParticipantsData(
    base: PacketBase,
    participantsChangedTimestamp: Long,
    name: Array[String]
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Participant timings data.  
//
//	Frequency: Each tick of the UDP streamer how it is set in the options.
//	When it is sent: in race
//
*******************************************************************************************************************/
object RaceState {
  val RACESTATE_INVALID = 0
  val RACESTATE_NOT_STARTED = 1
  val RACESTATE_RACING = 2
  val RACESTATE_FINISHED = 3
  val RACESTATE_DISQUALIFIED = 4
  val RACESTATE_RETIRED = 5
  val RACESTATE_DNF = 6
}

case class FormatParticipantInfo(
    worldPosition: Array[Short],
    orientation: Array[Short], // Quantized heading (-PI .. +PI) , Quantized pitch (-PI / 2 .. +PI / 2),  Quantized bank (-PI .. +PI).
    currentLapDistance: Int,
    racePosition: Short, // holds the race position, + top bit shows if the participant is active or not
    isActive: Boolean,
    sector: Short, // sector + extra precision bits for x/z position
    highestFlag: Short,
    pitModeSchedule: Short,
    carIndex: Int, // top bit shows if participant is (local or remote) human player or not
    raceState: Short, // race state flags + invalidated lap indication --
    currentLap: Short,
    currentTime: String, // [ Unit: Seconds ]
    currentSectorTime: String // [ Unit: Seconds ]
)

case class ParticipantInfo(
    worldPosition: Array[Short],
    orientation: Array[Short], // Quantized heading (-PI .. +PI) , Quantized pitch (-PI / 2 .. +PI / 2),  Quantized bank (-PI .. +PI).
    currentLapDistance: Int,
    racePosition: Short, // holds the race position, + top bit shows if the participant is active or not
    isActive: Boolean,
    sector: Short, // sector + extra precision bits for x/z position
    highestFlag: Short,
    pitModeSchedule: Short,
    carIndex: Int, // top bit shows if participant is (local or remote) human player or not
    raceState: Short, // race state flags + invalidated lap indication --
    currentLap: Short,
    currentTime: Float, // [ Unit: Seconds ]
    currentSectorTime: Float // [ Unit: Seconds ]
)

// partialPacketNumber = 1 Only
case class TimingsData(
    base: PacketBase,
    numParticipants: Byte,
    participantsChangedTimestamp: Long,
    eventTimeRemaining: String, // [ Unit: Seconds ] time remaining, -1 for invalid time,  -1 - laps remaining in lap based races  --
    splitTimeAhead: Float,
    splitTimeBehind: Float,
    splitTime: Float,
    partcipants: Array[ParticipantInfo],
    formatPartcipants: Array[FormatParticipantInfo]
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Game State. 
//
//	Frequency: Each 5s while being in Main Menu, Each 10s while being in race + on each change Main Menu<->Race several times.
//	the frequency in Race is increased in case of weather timer being faster  up to each 5s for 30x time progression
//	When it is sent: Always
//
*******************************************************************************************************************/
object GameStateDefine {
  val GAME_EXITED: Byte = 0
  val GAME_FRONT_END: Byte = 1
  val GAME_INGAME_PLAYING: Byte = 2
  val GAME_INGAME_PAUSED: Byte = 3
  val GAME_INGAME_INMENU_TIME_TICKING: Byte = 4
  val GAME_INGAME_RESTARTING: Byte = 5
  val GAME_INGAME_REPLAY: Byte = 6
  val GAME_FRONT_END_REPLAY: Byte = 7
  val GAME_UNKNOWN: Byte = 127
}

object GameStateDefineValue {
  val GAME_EXITED = GameState(GameStateDefine.GAME_EXITED, "GAME_EXITED")
  val GAME_FRONT_END = GameState(GameStateDefine.GAME_FRONT_END, "GAME_FRONT_END")
  val GAME_INGAME_PLAYING = GameState(GameStateDefine.GAME_INGAME_PLAYING, "GAME_INGAME_PLAYING")
  val GAME_INGAME_PAUSED = GameState(GameStateDefine.GAME_INGAME_PAUSED, "GAME_INGAME_PAUSED")
  val GAME_INGAME_INMENU_TIME_TICKING = GameState(GameStateDefine.GAME_INGAME_INMENU_TIME_TICKING, "GAME_INGAME_INMENU_TIME_TICKING")
  val GAME_INGAME_RESTARTING = GameState(GameStateDefine.GAME_INGAME_RESTARTING, "GAME_INGAME_RESTARTING")
  val GAME_INGAME_REPLAY = GameState(GameStateDefine.GAME_INGAME_REPLAY, "GAME_INGAME_REPLAY")
  val GAME_FRONT_END_REPLAY = GameState(GameStateDefine.GAME_FRONT_END_REPLAY, "GAME_FRONT_END_REPLAY")
  val GAME_UNKNOWN = GameState(GameStateDefine.GAME_UNKNOWN, "Unknown")
}

object SessionStateDefine {
  val SESSION_INVALID: Byte = 0
  val SESSION_PRACTICE: Byte = 1
  val SESSION_TEST: Byte = 2
  val SESSION_QUALIFY: Byte = 3
  val SESSION_FORMATION_LAP: Byte = 4
  val SESSION_RACE: Byte = 5
  val SESSION_TIME_ATTACK: Byte = 6
  val SESSION_UNKNOWN: Byte = 127
}

object SessionStateDefineValue {
  val SESSION_INVALID = SessionState(SessionStateDefine.SESSION_INVALID, "SESSION_INVALID")
  val SESSION_PRACTICE = SessionState(SessionStateDefine.SESSION_PRACTICE, "SESSION_PRACTICE")
  val SESSION_TEST = SessionState(SessionStateDefine.SESSION_TEST, "SESSION_TEST")
  val SESSION_QUALIFY = SessionState(SessionStateDefine.SESSION_QUALIFY, "SESSION_QUALIFY")
  val SESSION_FORMATION_LAP = SessionState(SessionStateDefine.SESSION_FORMATION_LAP, "SESSION_FORMATION_LAP")
  val SESSION_RACE = SessionState(SessionStateDefine.SESSION_RACE, "SESSION_RACE")
  val SESSION_TIME_ATTACK = SessionState(SessionStateDefine.SESSION_TIME_ATTACK, "SESSION_TIME_ATTACK")
  val SESSION_UNKNOWN = SessionState(SessionStateDefine.SESSION_UNKNOWN, "Unknown")
}

case class GameState(value: Byte, text: String)
case class SessionState(value: Byte, text: String)

// partialPacketNumber = 1 Only
case class GameStateData(
    base: PacketBase,
    buildVersionNumber: Int,
    gameState: GameState, // first 3 bits are used for game state enum, second 3 bits for session state enum See shared memory example file for the enums
    sessionState: SessionState,
    ambientTemperature: Byte,
    trackTemperature: Byte,
    rainDensity: Short,
    snowDensity: Short,
    windSpeed: Byte,
    windDirectionX: Byte,
    windDirectionY: Byte // 22 padded to 24
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Participant Stats and records
//
//	Frequency: When entering the race and each time any of the values change, so basically each time any of the participants
//						crosses a sector boundary.
//	When it is sent: In Race
//
*******************************************************************************************************************/
case class FormatParticipantStatsInfo(
    fastestLapTime: String, // [ Unit: Seconds ]
    lastLapTime: String, // [ Unit: Seconds ]
    lastSectorTime: String, // [ Unit: Seconds ]
    fastestSector1Time: String, // [ Unit: Seconds ]
    fastestSector2Time: String, // [ Unit: Seconds ]
    fastestSector3Time: String // [ Unit: Seconds ]
)

case class ParticipantStatsInfo(
    fastestLapTime: Float, // [ Unit: Seconds ]
    lastLapTime: Float, // [ Unit: Seconds ]
    lastSectorTime: Float, // [ Unit: Seconds ]
    fastestSector1Time: Float, // [ Unit: Seconds ]
    fastestSector2Time: Float, // [ Unit: Seconds ]
    fastestSector3Time: Float // [ Unit: Seconds ]
)

case class ParticipantsStats(
    participants: Array[ParticipantStatsInfo],
    formatParticipants: Array[FormatParticipantStatsInfo]
)

// partialPacketNumber = 1 Only
case class TimeStatsData(
    base: PacketBase,
    participantsChangedTimestamp: Long,
    stats: ParticipantsStats
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*******************************************************************************************************************
//
//	Participant Vehicle names
//
//	Frequency: Logarithmic decrease
//	When it is sent: Counter resets on entering InRace state and again each  the participants change. 
//	The sParticipantsChangedTimestamp represent last time the participants has changed and is  to be used to sync 
//	this information with the rest of the participant related packets
//
//	Note: This data is always sent with at least 2 packets. The 1-(n-1) holds the vehicle name for each participant
//	The last one holding the class names.
//
*******************************************************************************************************************/
case class VehicleInfo(
    index: Int,
    carClass: Long,
    name: String
)

// partialPacketNumber = 1 or 2, 3
// partialPacketIndex From 1 to partialPacketNumber - 1
case class ParticipantVehicleNamesData(
    base: PacketBase,
    vehicles: Array[VehicleInfo]
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

case class ClassInfo(
    classIndex: Long,
    name: String
)

// partialPacketNumber = 1 , 2 or 3
// Measurement is impossible because partialPacketIndex and partialPacketNumber are always the same.
case class VehicleClassNamesData(
    base: PacketBase,
    classes: Array[ClassInfo]
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}

/*
 * Time Data
 */
case class LapTime(
    lap: String,
    sector1: String,
    sector2: String,
    sector3: String,
    lapTime: String
)

case class LapTimeDetails(
    base: PacketBase,
    isTimedSessions: Boolean,
    lapsInEvent: Int,
    current: LapTime,
    fastest: LapTime,
    average: LapTime,
    history: List[LapTime]
) extends UdpData {
  def toJsonString: String = this.toJson.toString
}
