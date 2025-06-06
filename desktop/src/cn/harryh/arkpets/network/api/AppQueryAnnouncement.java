/** Copyright (c) 2022-2025, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.network.api;

import cn.harryh.arkpets.controllers.AnnounceDialog;
import cn.harryh.arkpets.utils.Logger;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;


public class AppQueryAnnouncement extends BaseModel<AppQueryAnnouncement.AppQueryAnnouncementData> {
    public static class AppQueryAnnouncementData implements Serializable {
        @JSONField
        public List<AnnounceItem> contents;
    }


    public static class AnnounceItem implements Serializable {
        /** @since ArkPets 3.7 */ @JSONField
        public String title;
        /** @since ArkPets 3.7 */ @JSONField
        public String date;
        /** @since ArkPets 3.7 */ @JSONField
        public String group;
        /** @since ArkPets 3.7 */ @JSONField
        public String markdown;
        /** @since ArkPets 3.7 */ @JSONField
        public String source;

        @JSONField(deserialize = false)
        public String getAnnoId() {
            return String.format("%08x", hashCode());
        }

        @JSONField(deserialize = false)
        public Instant getParsedDate() {
            try {
                return date != null ? Instant.parse(date) : null;
            } catch (DateTimeParseException e) {
                Logger.warn("Announce", "Unrecognized date string \"" + date + "\"");
                return null;
            }
        }

        @JSONField(deserialize = false)
        public AnnounceDialog.AnnounceGroup getParsedGroup() {
            try {
                return group != null ? AnnounceDialog.AnnounceGroup.valueOf(group) : null;
            } catch (IllegalArgumentException e) {
                Logger.warn("Announce", "Unrecognized group string \"" + group + "\"");
                return AnnounceDialog.AnnounceGroup.DEFAULT;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AnnounceItem that = (AnnounceItem) o;
            return Objects.equals(title, that.title) && Objects.equals(date, that.date)
                    && Objects.equals(markdown, that.markdown) && Objects.equals(source, that.source);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, date, group, markdown, source);
        }
    }
}
