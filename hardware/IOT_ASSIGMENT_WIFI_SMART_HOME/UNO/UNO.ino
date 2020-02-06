///////////////////////////////////////// Include Library  ///////////////////////////////////
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <Keypad.h>
#include <EEPROM.h>     // We are going to read and write PICC's UIDs from/to EEPROM
#include <SPI.h>        // RC522 Module uses SPI protocol
#include <MFRC522.h>  // Library for Mifare RC522 Devices

///////////////////////////////////////// Define LCD  ///////////////////////////////////
LiquidCrystal_I2C lcd(0x27, 16, 2);

///////////////////////////////////////// Define KeyBoard  ///////////////////////////////////
const byte numRows = 4; //number of rows on the keypad
const byte numCols = 3; //number of columns on the keypad
//keymap defines the key pressed according to the row and columns just as appears on the keypad
char keymap[numRows][numCols] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}
};
byte rowPins[numRows] = {2, 3, 4, 5};
byte colPins[numCols] = {6, 7, 8};
Keypad myKeypad = Keypad(makeKeymap(keymap), rowPins, colPins, numRows, numCols); //initializes an instance of the Keypad class


///////////////////////////////////////// Define Keypad Mode  ///////////////////////////////////
#define Password_Lenght 16 // Give enough room for six chars + NULL char
char Data[Password_Lenght]; // 6 is the number of chars it can hold + the null char = 7
char Master[7] = "011111";
char Deep_mode[Password_Lenght] = "0336471477";
byte data_count = 0, master_count = 0, mode = 0 ;
char password;
///////////////////////////////////////// Define RFID Mode  ///////////////////////////////////
constexpr uint8_t buzzer = A0;   // set buzzer pin
constexpr uint8_t lock = A1;     // Set Relay Pin
boolean match = false;          // initialize card match to false
boolean programMode = false;  // initialize programming mode to false
boolean replaceMaster = false;
uint8_t successRead;    // Variable integer to keep if we have Successful Read from Reader
byte storedCard[4];   // Stores an ID read from EEPROM
byte readCard[4];   // Stores scanned ID read from RFID Module
byte masterCard[4];   // Stores master card's ID read from EEPROM
// Create MFRC522 instance.
constexpr uint8_t RST_PIN = 9;     // Configurable, see typical pin layout above
constexpr uint8_t SS_PIN = 10;     // Configurable, see typical pin layout above
MFRC522 mfrc522(SS_PIN, RST_PIN);

///////////////////////////////////////// Set UP  ///////////////////////////////////
void setup() {
  pinMode(lock, OUTPUT);
  pinMode(buzzer, OUTPUT);
  digitalWrite(lock, HIGH);    // Make sure door is locked
  mode = 0;
  lcd.begin(); // initialize the LCD
  lcd.backlight(); // Turn on the blacklight and print a message.
  lcd.setCursor(0, 0);
  lcd.print("----IO13301----");
  lcd.setCursor(0, 1);
  lcd.print("-- ASSIGNMENT --");
  delay(1500);
  DLock_lcd();
  myKeypad.begin(makeKeymap(keymap));
  //Protocol Configuration
  Serial.begin(9600);  // Initialize serial communications with PC
  SPI.begin();           // MFRC522 Hardware uses SPI protocol
  mfrc522.PCD_Init();    // Initialize MFRC522 Hardware
  Serial.println(F("Access Control Example v0.1"));   // For debugging purposes
  ShowReaderDetails();  // Show details of PCD - MFRC522 Card Reader details

  ///////////////////////////////////////// Check Master Card //////////////////////////////////
  if (EEPROM.read(1) != 143) {
    Serial.println(F("No Master Card Defined"));
    Serial.println(F("Scan A PICC to Define as Master Card"));
    do {
      successRead = getID();            // sets successRead to 1 when we get read from reader otherwise 0
    }
    while (!successRead);                  // Program will not go further while you not get a successful read
    for ( uint8_t j = 0; j < 4; j++ ) {        // Loop 4 times
      EEPROM.write( 2 + j, readCard[j] );  // Write scanned PICC's UID to EEPROM, start from address 3
    }
    EEPROM.write(1, 143);                  // Write to EEPROM we defined Master Card.
    Serial.println(F("Master Card Defined"));
  }
  Serial.println(F("-------------------"));
  Serial.println(F("Master Card's UID"));
  for ( uint8_t i = 0; i < 4; i++ ) {          // Read Master Card's UID from EEPROM
    masterCard[i] = EEPROM.read(2 + i);    // Write it to masterCard
    Serial.print(masterCard[i], HEX);
  }
  Serial.println("");
  Serial.println(F("-------------------"));
  Serial.println(F("Everything is ready"));
  Serial.println(F("Waiting PICCs to be scanned"));
}

///////////////////////////////////////// Main Loop ///////////////////////////////////
void loop() {
  do {

    int data;
    if (Serial.available() > 0) {
      data = Serial.read();
      delay(200);
      //Serial.println(data);
      if (data == 111) {
        granted(5000);
      }
      }
      ///////////////////////////////////////// Keyboard Mode ///////////////////////////////////
      char keypressed = myKeypad.getKey();
      if (keypressed != NO_KEY) {
        if (mode == 0) {
          if (keypressed == '*') {
            mode = 1;
            lcd.backlight();
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print(" Enter Password");
          }
        } else if (keypressed == '#') {
          checkKPID();
          clearData();
        } else if (keypressed != '*') {
          Data[data_count] = keypressed; // store char into data array
          lcd.setCursor(data_count, 1); // move cursor to show each new char
          lcd.print('*'); // print char at said cursor Data[data_count]
          data_count++; // increment data array by 1 to store new char, also keep track of the number of chars entered
        } else {
          data_count -= 1;
          Data[data_count] = ' ';
          lcd.setCursor(data_count, 1);
          lcd.print(Data[data_count]); // print char at said cursor
        }
      }
    

    ///////////////////////////////////////// RFID Mode ///////////////////////////////////
    successRead = getID();  // sets successRead to 1 when we get read from reader otherwise 0
  }
  while (!successRead);   //the program will not go further while you are not getting a successful read
  if (programMode) {
    if ( isMaster(readCard) ) { //When in program mode check First If master card scanned again to exit program mode
      master_exit();
      Serial.println(F("Master Card Scanned"));
      Serial.println(F("Exiting Program Mode"));
      Serial.println(F("-----------------------------"));
      programMode = false;
      return;
    }
    else {
      if ( findID(readCard) ) { // If scanned card is known delete it
        Serial.println(F("I know this PICC, removing..."));
        deleteID(readCard);
        Serial.println("-----------------------------");
        Serial.println(F("Scan a PICC to ADD or REMOVE to EEPROM"));
      }
      else {                    // If scanned card is not known add it
        Serial.println(F("I do not know this PICC, adding..."));
        writeID(readCard);
        Serial.println(F("-----------------------------"));
        Serial.println(F("Scan a PICC to ADD or REMOVE to EEPROM"));
      }
    }
  }
  else {
    if ( isMaster(readCard)) {
      master_come();
      // If scanned card's ID matches Master Card's ID - enter program mode
      programMode = true;
      Serial.println(F("Hello Master - Entered Program Mode"));
      uint8_t count = EEPROM.read(0);   // Read the first Byte of EEPROM that
      Serial.print(F("I have "));     // stores the number of ID's in EEPROM
      Serial.print(count);
      Serial.print(F(" record(s) on EEPROM"));
      Serial.println("");
      Serial.println(F("Scan a PICC to ADD or REMOVE to EEPROM"));
      Serial.println(F("Scan Master Card again to Exit Program Mode"));
      Serial.println(F("-----------------------------"));
    }
    else {
      if ( findID(readCard) ) { // If not, see if the card is in the EEPROM
        Serial.println(F("WEllCOME HƠME"));
        granted(5000);         // Open the door lock for 300 ms
      }
      else {      // If not, show that the ID was not valid
        Serial.println(F("ACCESS DENIED"));
        denied();
      }
    }
  }
}

/////////////////////////////////////////  FUNCTION   ///////////////////////////////////

/////////////////////////////////////////  Keyboard_deep_mode     ///////////////////////////////////
void Deep_mode_kp() {
  SSound();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Deep Mode: ON");
  lcd.setCursor(0, 1);
  lcd.print("UPDATING SORRY@@");
  delay(1000);
  DLock_lcd();
}

/////////////////////////////////////////  equal ID    ///////////////////////////////////
void checkKPID() {

  if (strcmp(Data, Master) == 0) { // equal to (strcmp(Data, Master) == 0)
    granted(5000);
  }
  else if (strcmp(Data, Deep_mode) == 0) { // equal to (strcmp(Data, Master) == 0)
    Deep_mode_kp();
  } else {
    denied();
  } mode = 0;
}

/////////////////////////////////////////  Access Granted    ///////////////////////////////////
void granted ( uint16_t setDelay) {
  lcd.backlight();
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
  delay(100);
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("!!Successfully!!");
  lcd.setCursor(0, 1);
  lcd.print("!WELLCOME  HOME!");
  digitalWrite(lock, LOW);     // Unlock door!
  delay(setDelay);          // Hold door lock open for given seconds
  digitalWrite(lock, HIGH);    // Relock door
  DLock_lcd();
}

///////////////////////////////////////// lcd-door-lock  ///////////////////////////////////
void DLock_lcd() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Door Locking Now");
  lcd.noBacklight();
}

///////////////////////////////////////// Success sound  ///////////////////////////////////
void SSound() {
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
  delay(100);
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
  delay(300);
  digitalWrite(buzzer, HIGH);
  delay(200);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
}
///////////////////////////////////////// Denined sound  ///////////////////////////////////
void DSound() {
  digitalWrite(buzzer, HIGH);
  delay(500);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
}

///////////////////////////////////////// Access Denied  ///////////////////////////////////
void denied() {
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(" Access Denide!");
  lcd.setCursor(0, 1);
  lcd.print("   TRY AGAIN!   ");
  DSound();
  delay(1000);
  DLock_lcd();
}
///////////////////////////////////////// Master Access  ///////////////////////////////////
void master_come() {
  SSound();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Deep Mode: ON");
  lcd.setCursor(0, 1);
  lcd.print("Scan Again~Exit!");
}
///////////////////////////////////////// Master OUT  ///////////////////////////////////
void master_exit() {
  SSound();
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Exit Deep Mode");
  lcd.setCursor(0, 1);
  lcd.print("Deep Mode: OFF");
  delay(1000);
  DLock_lcd();
}

///////////////////////////////////////// Clear_Old_Password  ///////////////////////////////////
void clearData()
{
  while (data_count != 0)
  { // This can be used for any array size,
    Data[data_count--] = 0; //clear array for new data
  }
  return;
}


///////////////////////////////////////// Get PICC's UID ///////////////////////////////////
uint8_t getID() {
  // Getting ready for Reading PICCs
  if ( ! mfrc522.PICC_IsNewCardPresent()) { //If a new PICC placed to RFID reader continue
    return 0;
  }
  if ( ! mfrc522.PICC_ReadCardSerial()) {   //Since a PICC placed get Serial and continue
    return 0;
  }
  // There are Mifare PICCs which have 4 byte or 7 byte UID care if you use 7 byte PICC
  // I think we should assume every PICC as they have 4 byte UID
  // Until we support 7 byte PICCs
  Serial.println(F("Scanned PICC's UID:"));
  for ( uint8_t i = 0; i < 4; i++) {  //
    readCard[i] = mfrc522.uid.uidByte[i];
    Serial.print(readCard[i], HEX);
  }
  Serial.println("");
  mfrc522.PICC_HaltA(); // Stop reading
  return 1;
}

void ShowReaderDetails() {
  // Get the MFRC522 software version
  byte v = mfrc522.PCD_ReadRegister(mfrc522.VersionReg);
  Serial.print(F("MFRC522 Software Version: 0x"));
  Serial.print(v, HEX);
  if (v == 0x91)
    Serial.print(F(" = v1.0"));
  else if (v == 0x92)
    Serial.print(F(" = v2.0"));
  else
    Serial.print(F(" (unknown),probably a chinese clone?"));
  Serial.println("");
  // When 0x00 or 0xFF is returned, communication probably failed
  if ((v == 0x00) || (v == 0xFF)) {
    Serial.println(F("WARNING: Communication failure, is the MFRC522 properly connected?"));
    Serial.println(F("SYSTEM HALTED: Check connections."));
    // Visualize system is halted
    while (true); // do not go further
  }
}

//////////////////////////////////////// Normal Mode   ///////////////////////////////////
void normalModeOn () {

  digitalWrite(lock, HIGH);    // Make sure Door is Locked
}

//////////////////////////////////////// Read an ID from EEPROM //////////////////////////////
void readID( uint8_t number ) {
  uint8_t start = (number * 4 ) + 2;    // Figure out starting position
  for ( uint8_t i = 0; i < 4; i++ ) {     // Loop 4 times to get the 4 Bytes
    storedCard[i] = EEPROM.read(start + i);   // Assign values read from EEPROM to array
  }
}

///////////////////////////////////////// Add ID to EEPROM   ///////////////////////////////////
void writeID( byte a[] ) {
  if ( !findID( a ) ) {     // Before we write to the EEPROM, check to see if we have seen this card before!
    uint8_t num = EEPROM.read(0);     // Get the numer of used spaces, position 0 stores the number of ID cards
    uint8_t start = ( num * 4 ) + 6;  // Figure out where the next slot starts
    num++;                // Increment the counter by one
    EEPROM.write( 0, num );     // Write the new count to the counter
    for ( uint8_t j = 0; j < 4; j++ ) {   // Loop 4 times
      EEPROM.write( start + j, a[j] );  // Write the array values to EEPROM in the right position
    }
    successWrite();
    Serial.println(F("Succesfully added ID record to EEPROM"));
  }
  else {
    failedWrite();
    Serial.println(F("Failed! There is something wrong with ID or bad EEPROM"));
  }
}

///////////////////////////////////////// Remove ID from EEPROM   ///////////////////////////////////
void deleteID( byte a[] ) {
  if ( !findID( a ) ) {     // Before we delete from the EEPROM, check to see if we have this card!
    failedWrite();      // If not
    Serial.println(F("Failed! There is something wrong with ID or bad EEPROM"));
  }
  else {
    uint8_t num = EEPROM.read(0);   // Get the numer of used spaces, position 0 stores the number of ID cards
    uint8_t slot;       // Figure out the slot number of the card
    uint8_t start;      // = ( num * 4 ) + 6; // Figure out where the next slot starts
    uint8_t looping;    // The number of times the loop repeats
    uint8_t j;
    uint8_t count = EEPROM.read(0); // Read the first Byte of EEPROM that stores number of cards
    slot = findIDSLOT( a );   // Figure out the slot number of the card to delete
    start = (slot * 4) + 2;
    looping = ((num - slot) * 4);
    num--;      // Decrement the counter by one
    EEPROM.write( 0, num );   // Write the new count to the counter
    for ( j = 0; j < looping; j++ ) {         // Loop the card shift times
      EEPROM.write( start + j, EEPROM.read(start + 4 + j));   // Shift the array values to 4 places earlier in the EEPROM
    }
    for ( uint8_t k = 0; k < 4; k++ ) {         // Shifting loop
      EEPROM.write( start + j + k, 0);
    }
    successDelete();
    Serial.println(F("Succesfully removed ID record from EEPROM"));
  }
}

///////////////////////////////////////// Check Bytes   ///////////////////////////////////
boolean checkTwo ( byte a[], byte b[] ) {
  if ( a[0] != 0 )      // Make sure there is something in the array first
    match = true;       // Assume they match at first
  for ( uint8_t k = 0; k < 4; k++ ) {   // Loop 4 times
    if ( a[k] != b[k] )     // IF a != b then set match = false, one fails, all fail
      match = false;
  }
  if ( match ) {      // Check to see if if match is still true
    return true;      // Return true
  }
  else  {
    return false;       // Return false
  }
}

///////////////////////////////////////// Find Slot   ///////////////////////////////////
uint8_t findIDSLOT( byte find[] ) {
  uint8_t count = EEPROM.read(0);       // Read the first Byte of EEPROM that
  for ( uint8_t i = 1; i <= count; i++ ) {    // Loop once for each EEPROM entry
    readID(i);                // Read an ID from EEPROM, it is stored in storedCard[4]
    if ( checkTwo( find, storedCard ) ) {   // Check to see if the storedCard read from EEPROM
      // is the same as the find[] ID card passed
      return i;         // The slot number of the card
      break;          // Stop looking we found it
    }
  }
}

///////////////////////////////////////// Find ID From EEPROM   ///////////////////////////////////
boolean findID( byte find[] ) {
  uint8_t count = EEPROM.read(0);     // Read the first Byte of EEPROM that
  for ( uint8_t i = 1; i <= count; i++ ) {    // Loop once for each EEPROM entry
    readID(i);          // Read an ID from EEPROM, it is stored in storedCard[4]
    if ( checkTwo( find, storedCard ) ) {   // Check to see if the storedCard read from EEPROM
      return true;
      break;  // Stop looking we found it
    }
    else {    // If not, return false
    }
  }
  return false;
}

///////////////////////////////////////// Write Success to EEPROM   ///////////////////////////////////
// Flashes the green LED 3 times to indicate a successful write to EEPROM
void successWrite() {
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
}

///////////////////////////////////////// Write Failed to EEPROM   ///////////////////////////////////
// Flashes the red LED 3 times to indicate a failed write to EEPROM
void failedWrite() {
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
  delay(100);
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
}

///////////////////////////////////////// Success Remove UID From EEPROM  ///////////////////////////////////
// Flashes the blue LED 3 times to indicate a success delete to EEPROM
void successDelete() {
  digitalWrite(buzzer, HIGH);
  delay(100);    // Turn on green buzzer
  digitalWrite(buzzer, LOW);
}

////////////////////// Check readCard IF is masterCard   ///////////////////////////////////
// Check to see if the ID passed is the master programing card
boolean isMaster( byte test[] ) {
  if ( checkTwo( test, masterCard ) )
    return true;
  else
    return false;
}

// function that executes whenever data is requested from master
