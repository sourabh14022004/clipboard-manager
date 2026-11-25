package com.clipboardmanager.app.`data`.local

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.clipboardmanager.app.`data`.model.CardColor
import com.clipboardmanager.app.`data`.model.ClipType
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ClipDao_Impl(
  __db: RoomDatabase,
) : ClipDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfClipEntity: EntityInsertAdapter<ClipEntity>

  private val __clipConverters: ClipConverters = ClipConverters()

  private val __updateAdapterOfClipEntity: EntityDeleteOrUpdateAdapter<ClipEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfClipEntity = object : EntityInsertAdapter<ClipEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `clips` (`id`,`title`,`content`,`checklistJson`,`clipType`,`cardColor`,`isPinned`,`isFavorite`,`isLocked`,`sourceApp`,`createdAt`,`updatedAt`,`copyCount`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ClipEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.checklistJson)
        val _tmp: String = __clipConverters.fromClipType(entity.clipType)
        statement.bindText(5, _tmp)
        val _tmp_1: String = __clipConverters.fromCardColor(entity.cardColor)
        statement.bindText(6, _tmp_1)
        val _tmp_2: Int = if (entity.isPinned) 1 else 0
        statement.bindLong(7, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.isFavorite) 1 else 0
        statement.bindLong(8, _tmp_3.toLong())
        val _tmp_4: Int = if (entity.isLocked) 1 else 0
        statement.bindLong(9, _tmp_4.toLong())
        statement.bindText(10, entity.sourceApp)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.updatedAt)
        statement.bindLong(13, entity.copyCount.toLong())
      }
    }
    this.__updateAdapterOfClipEntity = object : EntityDeleteOrUpdateAdapter<ClipEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `clips` SET `id` = ?,`title` = ?,`content` = ?,`checklistJson` = ?,`clipType` = ?,`cardColor` = ?,`isPinned` = ?,`isFavorite` = ?,`isLocked` = ?,`sourceApp` = ?,`createdAt` = ?,`updatedAt` = ?,`copyCount` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ClipEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.content)
        statement.bindText(4, entity.checklistJson)
        val _tmp: String = __clipConverters.fromClipType(entity.clipType)
        statement.bindText(5, _tmp)
        val _tmp_1: String = __clipConverters.fromCardColor(entity.cardColor)
        statement.bindText(6, _tmp_1)
        val _tmp_2: Int = if (entity.isPinned) 1 else 0
        statement.bindLong(7, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.isFavorite) 1 else 0
        statement.bindLong(8, _tmp_3.toLong())
        val _tmp_4: Int = if (entity.isLocked) 1 else 0
        statement.bindLong(9, _tmp_4.toLong())
        statement.bindText(10, entity.sourceApp)
        statement.bindLong(11, entity.createdAt)
        statement.bindLong(12, entity.updatedAt)
        statement.bindLong(13, entity.copyCount.toLong())
        statement.bindLong(14, entity.id)
      }
    }
  }

  public override suspend fun insert(clip: ClipEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfClipEntity.insertAndReturnId(_connection, clip)
    _result
  }

  public override suspend fun insertAll(clips: List<ClipEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfClipEntity.insert(_connection, clips)
  }

  public override suspend fun update(clip: ClipEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfClipEntity.handle(_connection, clip)
  }

  public override fun observeAllClips(): Flow<List<ClipEntity>> {
    val _sql: String = "SELECT * FROM clips ORDER BY isPinned DESC, updatedAt DESC"
    return createFlow(__db, false, arrayOf("clips")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfChecklistJson: Int = getColumnIndexOrThrow(_stmt, "checklistJson")
        val _columnIndexOfClipType: Int = getColumnIndexOrThrow(_stmt, "clipType")
        val _columnIndexOfCardColor: Int = getColumnIndexOrThrow(_stmt, "cardColor")
        val _columnIndexOfIsPinned: Int = getColumnIndexOrThrow(_stmt, "isPinned")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfCopyCount: Int = getColumnIndexOrThrow(_stmt, "copyCount")
        val _result: MutableList<ClipEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClipEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpChecklistJson: String
          _tmpChecklistJson = _stmt.getText(_columnIndexOfChecklistJson)
          val _tmpClipType: ClipType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfClipType)
          _tmpClipType = __clipConverters.toClipType(_tmp)
          val _tmpCardColor: CardColor
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCardColor)
          _tmpCardColor = __clipConverters.toCardColor(_tmp_1)
          val _tmpIsPinned: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPinned).toInt()
          _tmpIsPinned = _tmp_2 != 0
          val _tmpIsFavorite: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp_3 != 0
          val _tmpIsLocked: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_4 != 0
          val _tmpSourceApp: String
          _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpCopyCount: Int
          _tmpCopyCount = _stmt.getLong(_columnIndexOfCopyCount).toInt()
          _item =
              ClipEntity(_tmpId,_tmpTitle,_tmpContent,_tmpChecklistJson,_tmpClipType,_tmpCardColor,_tmpIsPinned,_tmpIsFavorite,_tmpIsLocked,_tmpSourceApp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCopyCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllClipsOnce(): List<ClipEntity> {
    val _sql: String = "SELECT * FROM clips ORDER BY isPinned DESC, updatedAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfChecklistJson: Int = getColumnIndexOrThrow(_stmt, "checklistJson")
        val _columnIndexOfClipType: Int = getColumnIndexOrThrow(_stmt, "clipType")
        val _columnIndexOfCardColor: Int = getColumnIndexOrThrow(_stmt, "cardColor")
        val _columnIndexOfIsPinned: Int = getColumnIndexOrThrow(_stmt, "isPinned")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfCopyCount: Int = getColumnIndexOrThrow(_stmt, "copyCount")
        val _result: MutableList<ClipEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ClipEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpChecklistJson: String
          _tmpChecklistJson = _stmt.getText(_columnIndexOfChecklistJson)
          val _tmpClipType: ClipType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfClipType)
          _tmpClipType = __clipConverters.toClipType(_tmp)
          val _tmpCardColor: CardColor
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCardColor)
          _tmpCardColor = __clipConverters.toCardColor(_tmp_1)
          val _tmpIsPinned: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPinned).toInt()
          _tmpIsPinned = _tmp_2 != 0
          val _tmpIsFavorite: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp_3 != 0
          val _tmpIsLocked: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_4 != 0
          val _tmpSourceApp: String
          _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpCopyCount: Int
          _tmpCopyCount = _stmt.getLong(_columnIndexOfCopyCount).toInt()
          _item =
              ClipEntity(_tmpId,_tmpTitle,_tmpContent,_tmpChecklistJson,_tmpClipType,_tmpCardColor,_tmpIsPinned,_tmpIsFavorite,_tmpIsLocked,_tmpSourceApp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCopyCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getClipById(id: Long): ClipEntity? {
    val _sql: String = "SELECT * FROM clips WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfChecklistJson: Int = getColumnIndexOrThrow(_stmt, "checklistJson")
        val _columnIndexOfClipType: Int = getColumnIndexOrThrow(_stmt, "clipType")
        val _columnIndexOfCardColor: Int = getColumnIndexOrThrow(_stmt, "cardColor")
        val _columnIndexOfIsPinned: Int = getColumnIndexOrThrow(_stmt, "isPinned")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfCopyCount: Int = getColumnIndexOrThrow(_stmt, "copyCount")
        val _result: ClipEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpChecklistJson: String
          _tmpChecklistJson = _stmt.getText(_columnIndexOfChecklistJson)
          val _tmpClipType: ClipType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfClipType)
          _tmpClipType = __clipConverters.toClipType(_tmp)
          val _tmpCardColor: CardColor
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCardColor)
          _tmpCardColor = __clipConverters.toCardColor(_tmp_1)
          val _tmpIsPinned: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPinned).toInt()
          _tmpIsPinned = _tmp_2 != 0
          val _tmpIsFavorite: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp_3 != 0
          val _tmpIsLocked: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_4 != 0
          val _tmpSourceApp: String
          _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpCopyCount: Int
          _tmpCopyCount = _stmt.getLong(_columnIndexOfCopyCount).toInt()
          _result =
              ClipEntity(_tmpId,_tmpTitle,_tmpContent,_tmpChecklistJson,_tmpClipType,_tmpCardColor,_tmpIsPinned,_tmpIsFavorite,_tmpIsLocked,_tmpSourceApp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCopyCount)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLatestClip(): ClipEntity? {
    val _sql: String = "SELECT * FROM clips ORDER BY updatedAt DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfChecklistJson: Int = getColumnIndexOrThrow(_stmt, "checklistJson")
        val _columnIndexOfClipType: Int = getColumnIndexOrThrow(_stmt, "clipType")
        val _columnIndexOfCardColor: Int = getColumnIndexOrThrow(_stmt, "cardColor")
        val _columnIndexOfIsPinned: Int = getColumnIndexOrThrow(_stmt, "isPinned")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfCopyCount: Int = getColumnIndexOrThrow(_stmt, "copyCount")
        val _result: ClipEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpChecklistJson: String
          _tmpChecklistJson = _stmt.getText(_columnIndexOfChecklistJson)
          val _tmpClipType: ClipType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfClipType)
          _tmpClipType = __clipConverters.toClipType(_tmp)
          val _tmpCardColor: CardColor
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCardColor)
          _tmpCardColor = __clipConverters.toCardColor(_tmp_1)
          val _tmpIsPinned: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPinned).toInt()
          _tmpIsPinned = _tmp_2 != 0
          val _tmpIsFavorite: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp_3 != 0
          val _tmpIsLocked: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_4 != 0
          val _tmpSourceApp: String
          _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpCopyCount: Int
          _tmpCopyCount = _stmt.getLong(_columnIndexOfCopyCount).toInt()
          _result =
              ClipEntity(_tmpId,_tmpTitle,_tmpContent,_tmpChecklistJson,_tmpClipType,_tmpCardColor,_tmpIsPinned,_tmpIsFavorite,_tmpIsLocked,_tmpSourceApp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCopyCount)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findByContent(content: String): ClipEntity? {
    val _sql: String = "SELECT * FROM clips WHERE content = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, content)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfChecklistJson: Int = getColumnIndexOrThrow(_stmt, "checklistJson")
        val _columnIndexOfClipType: Int = getColumnIndexOrThrow(_stmt, "clipType")
        val _columnIndexOfCardColor: Int = getColumnIndexOrThrow(_stmt, "cardColor")
        val _columnIndexOfIsPinned: Int = getColumnIndexOrThrow(_stmt, "isPinned")
        val _columnIndexOfIsFavorite: Int = getColumnIndexOrThrow(_stmt, "isFavorite")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfSourceApp: Int = getColumnIndexOrThrow(_stmt, "sourceApp")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _columnIndexOfCopyCount: Int = getColumnIndexOrThrow(_stmt, "copyCount")
        val _result: ClipEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpChecklistJson: String
          _tmpChecklistJson = _stmt.getText(_columnIndexOfChecklistJson)
          val _tmpClipType: ClipType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfClipType)
          _tmpClipType = __clipConverters.toClipType(_tmp)
          val _tmpCardColor: CardColor
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfCardColor)
          _tmpCardColor = __clipConverters.toCardColor(_tmp_1)
          val _tmpIsPinned: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPinned).toInt()
          _tmpIsPinned = _tmp_2 != 0
          val _tmpIsFavorite: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsFavorite).toInt()
          _tmpIsFavorite = _tmp_3 != 0
          val _tmpIsLocked: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_4 != 0
          val _tmpSourceApp: String
          _tmpSourceApp = _stmt.getText(_columnIndexOfSourceApp)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          val _tmpCopyCount: Int
          _tmpCopyCount = _stmt.getLong(_columnIndexOfCopyCount).toInt()
          _result =
              ClipEntity(_tmpId,_tmpTitle,_tmpContent,_tmpChecklistJson,_tmpClipType,_tmpCardColor,_tmpIsPinned,_tmpIsFavorite,_tmpIsLocked,_tmpSourceApp,_tmpCreatedAt,_tmpUpdatedAt,_tmpCopyCount)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countClips(): Int {
    val _sql: String = "SELECT COUNT(*) FROM clips"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM clips WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteUnlockedNonPinnedHistory(): Int {
    val _sql: String = "DELETE FROM clips WHERE isPinned = 0 AND isFavorite = 0 AND isLocked = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun trimOldHistory(keepCount: Int) {
    val _sql: String = """
        |
        |        DELETE FROM clips WHERE id IN (
        |            SELECT id FROM clips
        |            WHERE isPinned = 0 AND isFavorite = 0 AND isLocked = 0
        |            ORDER BY updatedAt DESC
        |            LIMIT -1 OFFSET ?
        |        )
        |        
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, keepCount.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
