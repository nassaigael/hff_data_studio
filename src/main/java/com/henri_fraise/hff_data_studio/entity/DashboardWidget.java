package com.henri_fraise.hff_data_studio.entity;

import com.henri_fraise.hff_data_studio.enums.WidgetType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "dashboard_widget")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardWidget {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "widget_id")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dashboard_id", nullable = false)
	private Dashboard dashboard;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private WidgetType type;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "position", nullable = false)
	@Builder.Default
	private Integer position = 0;

	@Column(name = "row_index", nullable = false)
	@Builder.Default
	private Integer rowIndex = 0;

	@Column(name = "col_index", nullable = false)
	@Builder.Default
	private Integer colIndex = 0;

	@Column(name = "width", nullable = false)
	@Builder.Default
	private Integer width = 4;

	@Column(name = "height", nullable = false)
	@Builder.Default
	private Integer height = 3;

	@Column(name = "config_json", columnDefinition = "TEXT")
	private String configJson;

	@Column(name = "data_source_type", length = 50)
	private String dataSourceType;

	@Column(name = "data_source_id")
	private UUID dataSourceId;
}