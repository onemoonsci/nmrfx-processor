from itertools import izip
import array
import os
from shutil import copyfile
from org.nmrfx.processor.datasets import Dataset
from org.nmrfx.processor.datasets.peaks import PeakPickParameters
from org.nmrfx.peaks import PeakList
from org.nmrfx.processor.datasets.peaks import PeakPicker
from org.nmrfx.processor.datasets.vendor import NMRPipeData
from org.nmrfx.processor.math import Vec

class NMRFxDatasetScripting:
    dimNames = ['x','y','z','a','b','c','d']
    def __init__(self):
        self.cmd = Dataset

    def open(self, fileName,writable=False):
        fileName = os.path.join(os.getcwd(), fileName)
        dataset = Dataset(fileName,"",writable, False)
        return dataset

    def names(self):
        return  Dataset.names()

    def datasets(self):
        return  Dataset.datasets()

    def get(self, datasetName):
        dataset = Dataset.getDataset(datasetName)
        return dataset

    def create(self, fileName, sizes, srcDataset=None, title=""):
        Dataset.createDataset(fileName, "", sizes, True) 
        dataset = self.open(fileName, True)
        if srcDataset:
            nDim = len(sizes)
            nDimSrc = srcDataset.getNDim()
            for iDim in range(min(nDim,nDimSrc)):
                srcDataset.copyHeader(dataset, iDim)
            dataset.writeHeader();
            
        return dataset

    def createSub(self, fileName, nDim, srcDataset, title=""):
        if isinstance(srcDataset,basestring):
            srcDataset = nd.open(srcDataset,False)
        sizes = []
        nDimSrc = srcDataset.getNDim()
        if nDim > nDimSrc:
            raise Exception("New dataset has more dimensions than source")
        for iDim in range(nDim):
            sizes.append(srcDataset.getSize(iDim))

        Dataset.createDataset(fileName, "", sizes, True) 
        dataset = self.open(fileName, True)
        for iDim in range(nDim):
            srcDataset.copyHeader(dataset, iDim)
        dataset.writeHeader();
            
        return dataset

    def getVector(self, dataset, iDim):
        vec = Vec(dataset.getSize(iDim), dataset.getComplex(iDim))
        return vec

    def extract(self, dataset, iDim, *indices):
        vec = Vec(dataset.getSize(iDim), dataset.getComplex(iDim))
        dataset.readVector(vec, indices, iDim)
        extension = '_d'+str(iDim+1)
        for index in indices:
            extension += '_'+str(index+1)
        vec.setName(dataset.getName()[0:-3]+extension)
        newData = Dataset(vec)
        return newData

    def toPipe(self, dataset, fileName):
        np = NMRPipeData(dataset)
        np.saveFile(fileName)

    def combine(self, func, outName, dIn1, dIn2):
        copyfile(dIn1.getCanonicalFile(), outName)
        dOut = self.open(outName,True)
        iDim = 0
        for (vec1,vec2) in izip(dIn1.vectors(iDim), dIn2.vectors(iDim)):
            vec3 = func(vec1,vec2)
            dOut.writeVector(vec3)
        dOut.close()

    def combineN(self, func, outName, *datasets):
        nd = NMRFxDatasetScripting()
        useDatasets = []  
        for i,dataset in enumerate(datasets):
            if isinstance(dataset,basestring):
                dataset = nd.open(dataset,False)
            useDatasets.append(dataset)

        iDim = 0
        copyfile(useDatasets[0].getCanonicalFile(), outName)
        dOut = self.open(outName,True)
        nDim = useDatasets[0].getNDim()
        dim = array.array('i',range(0,nDim))
        vecs = []
        for i,dataset in enumerate(useDatasets):
            vec = Vec(useDatasets[0].getSize(iDim))
            vecs.append(vec)
        for vIndex in useDatasets[0].indexer(iDim):
            for i,dataset in enumerate(useDatasets):
                vecs[i].setPt(vIndex, dim)
                dataset.readVector(vecs[i])
            vec3 = func(*vecs)
            dOut.writeVector(vec3)
        dOut.close()

    def applyToValues(self, d, iDim, f):
        if isinstance(d,basestring):
            d = cmd.getDataset(d)
        values = d.getValues(iDim)
        values = [f(v) for v in values]
        d.setValues(iDim, values)

    def pick(self, dataset, listName=None, level=1.0, mode="new", region="box",pos=True, neg=False, **kwargs):
        if isinstance(dataset,basestring):
            dataset = self.get(dataset)
        if listName == None:
           listName = PeakList.getNameForDataset(dataset.getName()) 
        peakPickPar = PeakPickParameters(dataset, listName).mode(mode).region(region).pos(pos).neg(neg).level(level)
        peakPickPar.calcRange()
        for dim in kwargs:
            (lim1,lim2) = kwargs[dim]
            iDim = NMRFxDatasetScripting.dimNames.index(dim)
            peakPickPar = peakPickPar.limit(iDim,lim1,lim2)

        peakPicker = PeakPicker(peakPickPar)
        peakList = peakPicker.peakPick()
      
        return peakList

        
nd = NMRFxDatasetScripting()
