package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import java.awt.GridBagConstraints
import java.awt.Insets

class GridBagConstraintsBuilder {

    private var gridX: Int? = null
    private var gridY: Int? = null
    private var gridWidth: Int? = null
    private var gridHeight: Int? = null
    private var weightX: Double? = null
    private var weightY: Double? = null
    private var anchor: Int? = null
    private var fill: Int? = null
    private var insets: Insets? = null
    private var iPadX: Int? = null
    private var iPadY: Int? = null

    fun setGridX(gridX: Int?): GridBagConstraintsBuilder {
        this.gridX = gridX

        return this
    }

    fun setGridY(gridY: Int?): GridBagConstraintsBuilder {
        this.gridY = gridY

        return this
    }

    fun setGridWidth(gridWidth: Int?): GridBagConstraintsBuilder {
        this.gridWidth = gridWidth

        return this
    }

    fun setGridHeight(gridHeight: Int?): GridBagConstraintsBuilder {
        this.gridHeight = gridHeight

        return this
    }

    fun setWeightX(weightX: Double?): GridBagConstraintsBuilder {
        this.weightX = weightX

        return this
    }

    fun setWeightY(weightY: Double?): GridBagConstraintsBuilder {
        this.weightY = weightY

        return this
    }

    fun setAnchor(anchor: Int?): GridBagConstraintsBuilder {
        this.anchor = anchor

        return this
    }

    fun setFill(fill: Int?): GridBagConstraintsBuilder {
        this.fill = fill

        return this
    }

    fun setInsets(insets: Insets?): GridBagConstraintsBuilder {
        this.insets = insets?.let { Insets(it.top, it.left, it.bottom, it.right) }

        return this
    }

    fun setIPadX(iPadX: Int?): GridBagConstraintsBuilder {
        this.iPadX = iPadX

        return this
    }

    fun setIPadY(iPadY: Int?): GridBagConstraintsBuilder {
        this.iPadY = iPadY

        return this
    }

    fun build(): GridBagConstraints {
        val gbc = GridBagConstraints()

        gridX?.let { gbc.gridx = it }
        gridY?.let { gbc.gridy = it }
        gridWidth?.let { gbc.gridwidth = it }
        gridHeight?.let { gbc.gridheight = it }
        weightX?.let { gbc.weightx = it }
        weightY?.let { gbc.weighty = it }
        anchor?.let { gbc.anchor = it }
        fill?.let { gbc.fill = it }
        insets?.let { gbc.insets = it }
        iPadX?.let { gbc.ipadx = it }
        iPadY?.let { gbc.ipady = it }

        return gbc
    }
}